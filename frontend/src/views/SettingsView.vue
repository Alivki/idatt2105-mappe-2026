<script setup lang="ts">
import {computed} from 'vue'
import {useAuthStore} from '@/stores/auth'
import AppLayout from '@/components/layout/AppLayout.vue'
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'
import Button from '@/components/ui/button/Button.vue'
import Switch from '@/components/ui/switch/Switch.vue'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogTrigger from '@/components/ui/alert-dialog/AlertDialogTrigger.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import {
  useUpdateEmailNotificationsMutation,
  useDeleteAccountMutation,
  useDeleteOrganizationMutation,
} from '@/composables/useSettings'

const auth = useAuthStore()

const isAdmin = auth.role === 'ADMIN'

const emailNotifications = computed(() => auth.user?.emailNotifications ?? true)

const updateEmailNotifications = useUpdateEmailNotificationsMutation()
const deleteAccount = useDeleteAccountMutation()
const deleteOrganization = useDeleteOrganizationMutation()

function handleToggleEmailNotifications(value: boolean) {
  updateEmailNotifications.mutate(value)
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger/>
        <Separator orientation="vertical" class="header-separator"/>
        <span class="page-title">Innstillinger</span>
      </div>
    </header>

    <div class="page-content">

      <section class="settings-section">
        <h2 class="section-title">E-postadresse</h2>

        <div class="setting-row">
          <div class="setting-info">
            <span class="setting-label">Kommunikasjons-e-poster</span>
            <span class="setting-desc">Motta varsler og viktige oppdateringer på e-post.</span>
          </div>
          <Switch
            :checked="emailNotifications"
            @update:checked="handleToggleEmailNotifications"
          />
        </div>
      </section>

      <Separator/>

      <section class="settings-section">
        <h2 class="section-title">Fjerning av konto</h2>
        <div class="button-row">
          <AlertDialog>
            <AlertDialogTrigger>
              <Button variant="destructive">Slett kontoen</Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Slett kontoen?</AlertDialogTitle>
                <AlertDialogDescription>
                  Er du sikker på at du vil slette kontoen din? Dette kan ikke angres og all data
                  vil gå tapt permanent.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Avbryt</AlertDialogCancel>
                <AlertDialogAction variant="destructive" @click="deleteAccount.mutate()">
                  Ja, slett
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>
      </section>

      <template v-if="isAdmin">
        <Separator/>

        <section class="settings-section">
          <h2 class="section-title">Organisasjon</h2>
          <p class="section-desc">
            Hvis du sletter organisasjonen, vil alle medlemmer og tilhørende data bli permanent
            fjernet.
            Dette kan ikke angres.
          </p>
          <div class="button-row">
            <AlertDialog>
              <AlertDialogTrigger>
                <Button variant="destructive">Slett organisasjon</Button>
              </AlertDialogTrigger>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Slett organisasjon?</AlertDialogTitle>
                  <AlertDialogDescription>
                    Er du sikker på at du vil slette organisasjonen? Alle medlemmer og all
                    tilhørende data vil bli
                    permanent slettet. Dette kan ikke angres.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>Avbryt</AlertDialogCancel>
                  <AlertDialogAction variant="destructive" @click="deleteOrganization.mutate()">
                    Ja, slett organisasjon
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </div>
        </section>
      </template>

    </div>
  </AppLayout>
</template>

<style scoped>
.page-header {
  display: flex;
  height: 4rem;
  flex-shrink: 0;
  align-items: center;
}

.page-header-inner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0 1rem;
}

.header-separator {
  height: 1rem !important;
  width: 1px !important;
  margin-right: 0.5rem;
}

.page-title {
  font-weight: 500;
  color: hsl(var(--sidebar-primary, 245 43% 52%));
}

.page-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 0 1.5rem 2rem;
  max-width: 760px;
  width: 100%;
  margin: 0 auto;
}

.settings-section {
  padding: 1.75rem 0;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 0.5rem;
  color: hsl(var(--foreground));
}

.section-desc {
  font-size: 0.9rem;
  color: hsl(var(--muted-foreground));
  margin: 0 0 0.25rem;
  line-height: 1.5;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  padding: 1rem 0;
  border-bottom: 1px solid hsl(var(--border) / 0.5);
}

.setting-row:last-child {
  border-bottom: none;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.setting-label {
  font-size: 0.9375rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.setting-desc {
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
}

.button-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.625rem;
  margin-top: 0.75rem;
}
</style>
