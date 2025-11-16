import { ref, computed } from 'vue'
import { useStudentService } from '@/services/studentService'

export const useStudentProfile = () => {
  const { studentProfile, profileLoading, profileError, fetchMyProfile, fetchMyCourses, fetchMySchedule } = useStudentService()

  const courses = computed(() => studentProfile.value?.courses ?? [])
  const schedule = computed(() => studentProfile.value?.schedule ?? [])

  const loadProfile = async () => {
    try {
      await fetchMyProfile()
      await fetchMyCourses()
      await fetchMySchedule()
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