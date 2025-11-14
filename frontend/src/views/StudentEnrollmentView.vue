<template>
  <AdminLayout>
    <div class="space-y-8">
      <SecretaryEnrollmentForm />
    </div>
  </AdminLayout>
</template>

<script setup>
import AdminLayout from '../components/ui/TopBar.vue'
import SecretaryEnrollmentForm from '@/components/features/secretary/SecretaryEnrollmentForm.vue'
</script>