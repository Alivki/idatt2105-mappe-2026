<script setup lang="ts">
import {Settings} from 'lucide-vue-next'
import {useRouter} from 'vue-router'

defineProps<{
  showHaccp: boolean
  showAlcoholPolicy: boolean
}>()

const router = useRouter()
</script>

<template>
  <div v-if="showHaccp || showAlcoholPolicy" class="cta-card">
    <div class="cta-header">
      <div class="cta-icon">
        <Settings :size="20"/>
      </div>
      <div>
        <h3 class="cta-title">Kom i gang</h3>
        <p class="cta-subtitle">Fullfør oppsettet for full kontroll</p>
      </div>
    </div>

    <div class="cta-items">
      <div v-if="showHaccp" class="cta-item">
        <div class="cta-item-content">
          <strong>HACCP-oppsett</strong>
          <p>Konfigurer sjekklister basert på HACCP-analyse for å sikre mattrygghet og etterlevelse
            av regelverket.</p>
        </div>
        <button class="cta-btn" @click="router.push('/haccp-oppsett')">
          Sett opp
        </button>
      </div>

      <div v-if="showAlcoholPolicy" class="cta-item">
        <div class="cta-item-content">
          <strong>Skjenkepolicy</strong>
          <p>Definer rutiner for alderskontroll og ansvarlig alkoholhåndtering for å unngå
            prikker.</p>
        </div>
        <button class="cta-btn" @click="router.push('/skjenkepolicy')">
          Sett opp
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cta-card {
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-lg);
  background: var(--card-bg);
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cta-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cta-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--brand-soft);
  color: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cta-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.cta-subtitle {
  margin: 2px 0 0;
  font-size: 0.82rem;
  color: hsl(var(--muted-foreground));
}

.cta-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cta-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px;
  background: hsl(var(--secondary));
  border-radius: var(--radius-md);
}

.cta-item-content {
  flex: 1;
  min-width: 0;
}

.cta-item-content strong {
  font-size: 0.9rem;
  font-weight: 600;
}

.cta-item-content p {
  margin: 4px 0 0;
  font-size: 0.78rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.4;
}

.cta-btn {
  padding: 8px 16px;
  font-size: 0.82rem;
  font-weight: 600;
  border: none;
  background: var(--brand);
  color: #fff;
  border-radius: calc(var(--radius-md) - 1px);
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 150ms ease;
}

.cta-btn:hover {
  opacity: 0.9;
}

.cta-btn:active {
  transform: scale(0.97);
}

@media (max-width: 640px) {
  .cta-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .cta-btn {
    align-self: flex-start;
  }
}
</style>
