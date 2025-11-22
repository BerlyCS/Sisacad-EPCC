# Professor Grading Interface Design

## 1. Objectives and Constraints
- **Audience:** professors grading theory sections across multiple groups (A, B, C, etc.) within the same course in `frontend/src/views/ProfessorGradesView.vue`.
- **Capacity:** up to **150 students per course group**. A professor may teach several groups concurrently; UI must surface group context and remain performant when showing multiple groups (e.g., 2 × 150 rows) via virtualization.
- **Lab restriction:** professors assigned only to lab groups have read-only access to grades; they coordinate with the primary theory instructor and cannot submit grades. Backend must enforce this and expose `canGrade` flags to the UI.
- **Clean architecture & SOLID:** follow existing layering (Vue + Pinia + composables on the frontend, Spring Boot controllers/services/repositories on the backend). Keep modules modular, testable, and collaborative.
- **Experience goals:** minimize friction when reviewing rosters, editing grades, and accessing student details; provide quick navigation, inline profile previews, and consistent validation feedback.

## 2. Solution Overview
```
ProfessorGradesView (route)
  └─ GradeWorkspace (feature shell)
       ├─ GroupFilterTabs
       ├─ StudentListVirtual (virtualized roster per group)
       ├─ GradeEditorPanel (criteria form + submission status)
       └─ FeedbackDrawer / StudentQuickProfile
```
- **State orchestration:** `useProfessorGrades` composable + `frontend/src/stores/grades.ts` Pinia store manage course/group filters, roster, optimistic grade updates, and lab permissions.
- **Backend flow:** `GradeController` exposes roster, rubric, and grade submission endpoints; `GradeAssignmentService` encapsulates domain rules (group ownership, lab read-only, max student validations) atop repositories.
- **Data sync:** `gradeService.ts` fetches roster chunks (`size<=150`) and rubric metadata, caches results per course/group, and pushes grade mutations with optimistic rollback on failure.

## 3. Frontend Architecture (Vue 3 + Pinia)
### 3.1 Components
1. **GradeWorkspace.vue**
   - Layout container (responsive 2-column; collapses on mobile).
   - Receives `courseId`, `groupSummaries`, `labReadOnly`, `rubric` props; emits `reload`, `save-draft`, `submit-grade` events.
2. **GroupFilterTabs.vue**
   - Displays assigned groups (A, B, LAB) with counts; supports multi-select with chips.
   - Shows warning icon when a selected group is lab-only (read-only message).
3. **StudentListVirtual.vue**
   - Uses `vue-virtual-scroller` (or `@tanstack/vue-virtual`) to render up to 150 rows without scroll lag.
   - Columns: Name, CUI, Group, Submission status, Last updated, quick actions.
   - Row actions: select student, open quick profile, jump to Student Profile route (already defined in `BienvenidoView.vue`).
4. **GradeEditorPanel.vue**
   - Displays rubric-driven fields (per `rubric.criteria` definition) with inline validation.
   - Supports keyboard shortcuts: `Ctrl+S` save draft, `Ctrl+Enter` submit.
   - Shows computed final grade (using `useGradeCalculator`).
5. **FeedbackDrawer.vue** (optional slide-over)
   - Shows prior feedback, attachments, and allows comments when `canGrade` is true.
6. **StudentQuickProfile.vue**
   - Fetches data via existing `useStudentProfile` composable (already used in `BienvenidoView.vue`) to display contact info, schedule, and link to full profile page.

### 3.2 State & Composables
- **Store (`frontend/src/stores/grades.ts`) additions**
  - `selectedCourseId`, `selectedGroupIds` (array), `labReadOnly`, `rosterByGroup`, `gradeDrafts`, `submissionStatusMap`, `errorState`.
  - Actions: `setCourseAndGroups`, `loadRoster`, `loadRubric`, `saveDraft`, `submitGrade`, `bulkSaveDrafts`, `setLabReadOnly`.
  - Getters: `visibleStudents`, `isReadOnly`, `groupOptions`.
- **Composable `useProfessorGrades.ts` enhancements**
  - Debounced roster loading on filter change.
  - Prefetch rubric + roster in parallel via `Promise.all`.
  - Expose convenience methods for components (e.g., `selectStudent(studentId)`, `submitCurrentGrade()`).
- **Service (`frontend/src/services/gradeService.ts`)**
  - New methods `fetchCourseGroups`, `fetchRoster`, `fetchRubric`, `submitGrade`, `saveDrafts` calling backend endpoints listed below.
- **Error handling**
  - Standardized `GradeError` shape (status, message, code) for toast notifications.
  - Auto-refresh roster every 60s (if tab focused) to keep statuses in sync.

### 3.3 UX Considerations
- Sticky course header with group chips and lab warning.
- Instant search/filter (client-side) by student name/CUI; server filtering for large results via query params (`status`, `group`, `text`).
- Loading skeletons and optimistic UI when posting grades; revert state if backend rejects (e.g., lab attempts).
- Accessibility: focus management when switching students, ARIA roles on table, keyboard shortcuts exposed via tooltip.
- Responsive: on small screens, list becomes accordion and grade form moves to top stacked layout.

## 4. Backend Architecture (Spring Boot)
### 4.1 Endpoints (`GradeController`)
| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/grades/courses/{courseId}/groups` | Returns groups assigned to authenticated professor with `groupId`, `name`, `type`, `studentCount`, `canGrade`. |
| GET | `/api/grades/courses/{courseId}/students` | Query params `groupIds`, `status`, `page`, `size (<=150)`; returns roster entries with `student`, `group`, `currentGrade`, `submissionStatus`, `canGrade`. |
| GET | `/api/grades/courses/{courseId}/rubric` | Provides grading criteria, weights, allowed ranges. |
| POST | `/api/grades/courses/{courseId}/students/{studentId}/groups/{groupId}` | Submit/update grade with payload `{ criteriaScores[], finalGrade, feedback, status }`. Rejects when `group.type == LAB`. |
| PUT | `/api/grades/courses/{courseId}/bulk-drafts` | Save multiple drafts in one call (max 20). |

### 4.2 Services
- **GradeAssignmentService**
  - Validates professor assignment (via `ProfessorCourseService`).
  - Checks group type (LAB → forbid submit, but allow read).
  - Computes final grade (delegate to `GradeComputationService`).
  - Persists via `GradeRepository` with optimistic locking (`@Version`).
- **RosterQueryService**
  - Joins `StudentCourse` + `CourseGroup` + `Grade` to build roster payload.
  - Stream/Pagination to keep memory bounded at 150 entries per call.
- **RubricService**
  - Provides rubric definitions per course/period, possibly from syllabus data or configuration table.

### 4.3 Data Model Updates
- **CourseGroup entity**
  - Fields: `courseId`, `groupId`, `groupLetter`, `type (THEORY|LAB)`, `maxCapacity`.
- **GradeAssignment entity**
  - Fields: `courseId`, `groupId`, `studentDocumento`, `criteriaScores (JSON)`, `finalGrade`, `status`, `lastUpdatedBy`, `version`.
- **DTOs**
  - `GroupSummaryResponse`, `RosterEntryResponse`, `RubricResponse`, `GradeSubmissionRequest/Response`.

### 4.4 Security & Authorization
- Reuse `AuthorizationService` to map authenticated user to professor.
- Controller methods annotated with custom `@RequiresProfessorAccess` (optional) or manual checks via `authorizationService.hasRole(authentication, UserRole.PROFESSOR)`.
- `GradeAssignmentService` ensures `canGrade` when `group.type != LAB` and professor is assigned to that course/group.
- Return HTTP 403 (AccessDeniedException) for lab-only submissions or unassigned groups, letting frontend show inline error.

## 5. Performance & Reliability
- **Server Pagination:** limit `size` to 150 per request; `groupIds` param can include multiple groups but backend still respects per-group capacity.
- **Virtualization:** ensure only visible rows render; fallback to paginated view if virtualization lib not available.
- **Caching:** Pinia cache keyed by `courseId+groupSet`; invalidated when grade submission succeeds or manual refresh triggered.
- **Optimistic Locking:** prevents lost updates when multiple instructors grade same student; frontend handles 409 conflict by refreshing row.
- **Background refresh:** Web timer or server-sent events (optional future) to update roster statuses without manual reloads.

## 6. Implementation Roadmap
1. **Backend**
   - Add `CourseGroup` metadata and group type flag to repositories.
   - Implement new endpoints with unit/integration tests (lab blocking, multi-group fetch, pagination).
   - Update security aspect or controller checks to use `AuthorizationService` for professor roles.
2. **Frontend**
   - Extend `gradeService.ts`, `grades.ts`, and `useProfessorGrades.ts` with new data shapes.
   - Build `GradeWorkspace` feature components with virtualization, quick profile drawer, lab warning UI.
   - Wire up keyboard shortcuts, optimistic updates, and error handling.
3. **Testing**
   - Backend: Spring Boot tests per endpoint + security cases.
   - Frontend: Vitest for store/composables; Cypress for E2E (load roster → submit grade → verify). Include lab-read-only scenario.
4. **Documentation & Training**
   - Update README/handbook with grading workflow and shortcuts.
   - Provide quickstart video or Loom for professors if possible.

## 7. Risks & Mitigations
- **Large combined rosters:** enforce 150 limit per group and encourage filtering; virtualization handles UI strain.
- **Lab misconfiguration:** add admin tooling to mark group types correctly; log lab submission attempts for auditing.
- **Offline/latency:** keep draft mode client-side with autosave; show retry banner if submissions fail.

## 8. Next Steps
- Confirm rubric data source (static config vs syllabus upload).
- Decide on virtualization library (team already uses any? adopt `vue-virtual-scroller`).
- Kick off backend endpoint development, then parallelize frontend once contracts are stable.
