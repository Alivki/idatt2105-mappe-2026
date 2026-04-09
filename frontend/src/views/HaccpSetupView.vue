<script setup lang="ts">
import axios from 'axios'
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {toast} from 'vue-sonner'
import {ListChecks, ArrowRight, RotateCcw, CircleCheck} from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import Button from '@/components/ui/button/Button.vue'
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'
import HaccpStepper from '@/components/haccp-setup/HaccpStepper.vue'
import Step1BusinessType from '@/components/haccp-setup/Step1BusinessType.vue'
import Step2FoodHandling from '@/components/haccp-setup/Step2FoodHandling.vue'
import Step3Premises from '@/components/haccp-setup/Step3Premises.vue'
import Step4RiskAssessment from '@/components/haccp-setup/Step4RiskAssessment.vue'
import Step5Summary from '@/components/haccp-setup/Step5Summary.vue'
import {useWizardState} from '@/composables/useWizardState'
import {useGenerateChecklistsMutation} from '@/composables/useHaccpSetup'
import {useChecklistsQuery} from '@/composables/useChecklists'

const router = useRouter()
const {
  currentStep,
  totalSteps,
  wizardData,
  haccpTrinn,
  canProceed,
  nextStep,
  prevStep,
  buildRequest,
  reset
} = useWizardState()
const generateMutation = useGenerateChecklistsMutation()
const checklistsQuery = useChecklistsQuery()

const wizardStarted = ref(false)

const hasWizardChecklists = computed(() => {
  const checklists = checklistsQuery.data.value ?? []
  return checklists.some((c) => c.source === 'HACCP_WIZARD')
})

const isLoading = computed(() => checklistsQuery.isLoading.value)

const showIntro = computed(() => !wizardStarted.value && !hasWizardChecklists.value && !isLoading.value)
const showCompleted = computed(() => !wizardStarted.value && hasWizardChecklists.value && !isLoading.value)
const showWizard = computed(() => wizardStarted.value)

function startWizard() {
  reset()
  wizardStarted.value = true
}

async function handleGenerate() {
  try {
    const request = buildRequest()
    const result = await generateMutation.mutateAsync(request)
    toast.success(`${result.summary.totalChecklists} sjekklister generert med ${result.summary.totalItems} kontrollpunkter`)
    wizardStarted.value = false
    router.push('/sjekklister')
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const message = error.response?.data?.error?.message
      if (typeof message === 'string' && message.trim().length > 0) {
        toast.error(message)
        return
      }
    }
    toast.error('Kunne ikke generere sjekklister. Prøv igjen.')
  }
}

const stepTitles = [
  'Klassifisering',
  'Grunnforutsetninger',
  'Flytskjema',
  'Fareanalyse',
  'Sjekklister',
]

const nextStepLabels = [
  'Neste: Grunnforutsetninger',
  'Neste: Flytskjema',
  'Neste: Fareanalyse',
  'Neste: Sjekklister',
]
</script>

<template>
  <AppLayout active-menu-item="HACCP-oppsett">
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger/>
        <Separator orientation="vertical" class="header-separator"/>
        <span class="page-title">HACCP-oppsett</span>
      </div>
    </header>

    <div class="page-content">
      <p v-if="isLoading" class="state-line">Laster...</p>

      <div v-else-if="showIntro" class="intro-wrapper">
        <div class="intro-icon">
          <ListChecks :size="40" :stroke-width="1.5" aria-hidden="true"/>
        </div>
        <h2 class="intro-heading">Sett opp HACCP-sjekklister</h2>
        <p class="intro-desc">
          Bruk veiviseren til å generere skreddersydde sjekklister basert på virksomheten din og
          kravene fra Mattilsynet.
          Vi stiller spørsmål om virksomhetstype, mathåndtering og risikonivå, og lager sjekklister
          tilpasset HACCP-trinnet ditt.
        </p>
        <div class="intro-features">
          <div class="intro-feature">
            <span class="feature-num">1</span>
            <span>Klassifiser virksomheten og finn HACCP-trinn</span>
          </div>
          <div class="intro-feature">
            <span class="feature-num">2</span>
            <span>Gå gjennom grunnforutsetninger og flytskjema</span>
          </div>
          <div class="intro-feature">
            <span class="feature-num">3</span>
            <span>Utfør fareanalyse og generer sjekklister</span>
          </div>
        </div>
        <Button class="intro-btn" @click="startWizard">
          Start HACCP-oppsett
          <ArrowRight :size="16" aria-hidden="true"/>
        </Button>
      </div>

      <div v-else-if="showCompleted" class="intro-wrapper">
        <div class="completed-icon">
          <CircleCheck :size="40" :stroke-width="1.5" aria-hidden="true"/>
        </div>
        <h2 class="intro-heading">HACCP-oppsett er fullført</h2>
        <p class="intro-desc">
          Sjekklistene er generert og klare til bruk. Du finner dem under
          <router-link to="/sjekklister" class="inline-link">Sjekklister</router-link>
          .
        </p>
        <div class="completed-tip">
          <strong>Tips:</strong> Vi anbefaler å kjøre HACCP-veiviseren på nytt minst én gang i året,
          eller når menyen endres vesentlig, for å sikre at sjekklistene er oppdaterte og i tråd med
          gjeldende krav.
        </div>
        <div class="completed-actions">
          <Button variant="outline" @click="router.push('/sjekklister')">
            Gå til sjekklister
          </Button>
          <Button @click="startWizard">
            <RotateCcw :size="16" aria-hidden="true"/>
            Kjør veiviseren på nytt
          </Button>
        </div>
      </div>

      <template v-else-if="showWizard">
        <div class="wizard-card">
          <HaccpStepper :current-step="currentStep" :total-steps="totalSteps"/>

          <div class="step-title">{{ stepTitles[currentStep - 1] }}</div>

          <div class="wizard-body">
            <Step1BusinessType v-if="currentStep === 1" v-model:wizard="wizardData"
                               :haccp-trinn="haccpTrinn"/>
            <Step2FoodHandling v-if="currentStep === 2" v-model:wizard="wizardData"/>
            <Step3Premises v-if="currentStep === 3" v-model:wizard="wizardData"/>
            <Step4RiskAssessment v-if="currentStep === 4" v-model:wizard="wizardData"/>
            <Step5Summary
              v-if="currentStep === 5"
              :wizard="wizardData"
              :haccp-trinn="haccpTrinn"
              :is-generating="generateMutation.isPending.value"
              @generate="handleGenerate"
            />
          </div>

          <div class="wizard-nav">
            <Button
              v-if="currentStep > 1"
              variant="outline"
              @click="prevStep"
            >
              Tilbake
            </Button>
            <div v-else/>

            <Button
              v-if="currentStep < totalSteps"
              :disabled="!canProceed"
              @click="nextStep"
            >
              {{ nextStepLabels[currentStep - 1] }}
            </Button>
            <Button
              v-else
              :disabled="generateMutation.isPending.value"
              @click="handleGenerate"
            >
              {{ generateMutation.isPending.value ? 'Genererer...' : 'Generer sjekklister' }}
            </Button>
          </div>
        </div>
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
  align-items: center;
  padding: 0 1rem 2rem;
}

.state-line {
  padding: 14px;
  border-radius: 0.5rem;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  width: 100%;
  max-width: 640px;
}

.intro-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  width: 100%;
  max-width: 820px;
  padding: 2.5rem 2rem;
  border: 1px solid hsl(var(--border));
  border-radius: 1rem;
  background: hsl(var(--card));
  box-shadow: 0 1px 3px 0 hsl(var(--foreground) / 0.04);
}

.intro-icon,
.completed-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 5rem;
  height: 5rem;
  border-radius: 1rem;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  margin-bottom: 1.25rem;
}

.intro-heading {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin: 0 0 0.5rem;
}

.intro-desc {
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
  max-width: 440px;
  margin-bottom: 1.5rem;
}

.intro-features {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
  margin-bottom: 1.5rem;
  width: 100%;
  max-width: 360px;
}

.intro-feature {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
  color: hsl(var(--foreground));
  text-align: left;
}

.feature-num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 50%;
  background: hsl(var(--muted));
  color: hsl(var(--foreground));
  font-weight: 600;
  font-size: 0.8125rem;
  flex-shrink: 0;
}

.intro-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.completed-tip {
  font-size: 0.8125rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
  background: hsl(var(--muted) / 0.5);
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
  margin-bottom: 1.5rem;
  text-align: left;
  width: 100%;
}

.completed-tip strong {
  color: hsl(var(--foreground));
}

.completed-actions {
  display: flex;
  gap: 0.5rem;
}

.inline-link {
  color: hsl(var(--primary));
  text-decoration: underline;
  text-underline-offset: 2px;
}

.wizard-card {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  width: 100%;
  max-width: 820px;
  padding: 1.75rem 2rem;
  border: 1px solid hsl(var(--border));
  border-radius: 1rem;
  background: hsl(var(--card));
  box-shadow: 0 1px 3px 0 hsl(var(--foreground) / 0.04);
}

.step-title {
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: hsl(var(--foreground));
}

.wizard-body {
  flex: 1;
}

.wizard-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid hsl(var(--border));
}

@media (max-width: 760px) {
  .wizard-card {
    padding: 1.25rem 1rem;
  }

  .intro-wrapper {
    padding: 1.5rem 1rem;
  }

  .completed-actions {
    flex-direction: column;
    width: 100%;
  }

  .wizard-nav {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .wizard-nav :deep(button) {
    font-size: 0.85rem;
  }
}
</style>
