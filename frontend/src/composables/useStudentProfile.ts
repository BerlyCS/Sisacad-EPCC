import { computed } from 'vue'
import { useStudentService } from '@/services/studentService'

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
    loadProfile
  }
}