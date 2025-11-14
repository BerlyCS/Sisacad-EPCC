import { ref, computed } from 'vue'
import { useStudentService } from '@/services/studentService'

export const useStudentProfile = () => {
  const { studentProfile, profileLoading, profileError, fetchStudentProfile } = useStudentService()

  const courses = computed(() => studentProfile.value?.courses ?? [])
  const schedule = computed(() => studentProfile.value?.schedule ?? [])

  const loadProfile = async (cui: string) => {
    if (!cui) return
    try {
      await fetchStudentProfile(cui)
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