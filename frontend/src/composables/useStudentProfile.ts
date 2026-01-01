import { computed } from 'vue'
import { useStudentService } from '@/services/studentService'

type CourseType = 'THEORY' | 'LAB' | 'PRACTICE'

const DAY_ORDER = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'] as const
const DAY_LABELS: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo'
}

const normalizeDay = (value?: string | null) => {
  if (!value) return ''
  return value
    .trim()
    .toUpperCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
}

const toDayIndex = (value?: string | null) => {
  const normalized = normalizeDay(value)
  const index = DAY_ORDER.indexOf(normalized as typeof DAY_ORDER[number])
  return index >= 0 ? index : -1
}

const toMinutes = (value?: string | null) => {
  if (!value) return 0
  const [hours, minutes] = value.split(':').map(part => Number(part))
  if (Number.isNaN(hours) || Number.isNaN(minutes)) {
    return 0
  }
  return hours * 60 + minutes
}

const formatStartsIn = (minutes: number) => {
  if (minutes <= 0) {
    return 'En curso'
  }
  if (minutes < 60) {
    return `En ${minutes} min`
  }
  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    const rem = minutes % 60
    return rem === 0 ? `En ${hours} h` : `En ${hours} h ${rem} min`
  }
  const days = Math.floor(minutes / 1440)
  const remHours = Math.round((minutes % 1440) / 60)
  return remHours > 0 ? `En ${days} d ${remHours} h` : `En ${days} d`
}

export const useStudentProfile = () => {
  const {
    studentProfile,
    profileLoading,
    profileError,
    fetchMyProfile,
    fetchMyCourses,
    fetchMySchedule,
    fetchStudentProfile,
    fetchStudentCourses,
    fetchStudentSchedule
  } = useStudentService()

  const courses = computed(() => studentProfile.value?.courses ?? [])
  const schedule = computed(() => studentProfile.value?.schedule ?? [])

  const courseStats = computed(() => {
    const courseList = courses.value
    const allGroups = courseList.flatMap(course => course.groups ?? [])

    const totalCredits = courseList.reduce((sum, course) => {
      const credits = Number(course.creditNumber ?? 0)
      return Number.isFinite(credits) ? sum + credits : sum
    }, 0)

    const labSections = allGroups.filter(group => group.courseType === 'LAB').length
    const practiceSections = allGroups.filter(group => group.courseType === 'PRACTICE').length
    const theorySections = allGroups.filter(group => group.courseType === 'THEORY').length

    return {
      totalCourses: courseList.length,
      totalCredits,
      labSections,
      practiceSections,
      theorySections
    }
  })

  const scheduleByDay = computed(() => {
    if (!schedule.value.length) {
      return []
    }

    const groups = DAY_ORDER.map(day => ({
      key: day,
      label: DAY_LABELS[day] ?? day,
      slots: [] as Array<{
        id: string
        courseName: string
        courseCode: number
        courseType: CourseType
        courseTypeLabel: string
        timeRange: string
        classroomName: string
        startMinutes: number
      }>
    }))

    const COURSE_TYPE_LABEL: Record<CourseType, string> = {
      THEORY: 'Teoría',
      LAB: 'Laboratorio',
      PRACTICE: 'Práctica'
    }

    schedule.value.forEach((entry, index) => {
      const groupIndex = toDayIndex(entry.dayOfWeek)
      if (groupIndex < 0) {
        return
      }
      const group = groups[groupIndex]
      const startMinutes = toMinutes(entry.startTime)
      group.slots.push({
        id: `${entry.courseId}-${entry.startTime}-${index}`,
        courseName: entry.courseName,
        courseCode: entry.courseCode,
        courseType: entry.courseType,
        courseTypeLabel: COURSE_TYPE_LABEL[entry.courseType as CourseType] ?? entry.courseType,
        timeRange: `${entry.startTime ?? '—'} - ${entry.endTime ?? '—'}`,
        classroomName: entry.classroomName || 'Sin aula asignada',
        startMinutes
      })
    })

    groups.forEach(group => {
      group.slots.sort((a, b) => a.startMinutes - b.startMinutes)
    })

    return groups.filter(group => group.slots.length > 0)
  })

  const nextSession = computed(() => {
    if (!schedule.value.length) {
      return null
    }

    const now = new Date()
    const todayIndex = (now.getDay() + 6) % 7 // Monday = 0
    const minutesNow = now.getHours() * 60 + now.getMinutes()

    let candidate: { entry: typeof schedule.value[number]; minutesUntil: number } | null = null

    schedule.value.forEach(entry => {
      const dayIndex = toDayIndex(entry.dayOfWeek)
      if (dayIndex < 0) {
        return
      }
      const startMinutes = toMinutes(entry.startTime)
      let deltaDays = (dayIndex - todayIndex + 7) % 7
      let minutesUntil = deltaDays * 24 * 60 + (startMinutes - minutesNow)
      if (deltaDays === 0 && startMinutes < minutesNow) {
        minutesUntil += 7 * 24 * 60
      }
      if (!candidate || minutesUntil < candidate.minutesUntil) {
        candidate = { entry, minutesUntil }
      }
    })

    if (!candidate) {
      return null
    }

    const { entry, minutesUntil } = candidate
    const normalizedDay = normalizeDay(entry.dayOfWeek)

    return {
      courseName: entry.courseName,
      courseCode: entry.courseCode,
      dayLabel: DAY_LABELS[normalizedDay] ?? entry.dayOfWeek ?? '',
      timeRange: `${entry.startTime ?? '—'} - ${entry.endTime ?? '—'}`,
      classroomName: entry.classroomName || 'Por asignar',
      courseType: entry.courseType,
      startsInLabel: formatStartsIn(minutesUntil)
    }
  })

  const loadProfile = async (cui?: string) => {
    try {
      if (cui) {
        await fetchStudentProfile(cui)
        await Promise.all([
          fetchStudentCourses(cui),
          fetchStudentSchedule(cui)
        ])
      } else {
        await fetchMyProfile()
        await Promise.all([fetchMyCourses(), fetchMySchedule()])
      }
    } catch (error) {
      console.error('Error loading student profile:', error)
    }
  }

  return {
    studentProfile,
    profileLoading,
    profileError,
    courses,
    schedule,
    courseStats,
    scheduleByDay,
    nextSession,
    loadProfile
  }
}