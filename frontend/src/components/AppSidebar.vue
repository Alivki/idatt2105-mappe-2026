<script setup lang="ts">
import {
  LayoutDashboard,
  ClipboardCheck,
  Thermometer,
  Refrigerator,
  GraduationCap,
  ScrollText,
  ShieldCheck,
  ListChecks,
  AlertTriangle,
  Users,
  Settings,
  FileText,
} from 'lucide-vue-next'

import NavMain from '@/components/NavMain.vue'
import NavNotifications from '@/components/NavNotifications.vue'
import NavSecondary from '@/components/NavSecondary.vue'
import NavUser from '@/components/NavUser.vue'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarSeparator,
} from '@/components/ui/sidebar'
import {useAuthStore} from '@/stores/auth'
import {computed} from 'vue'
import veraIcon from '@/assets/vera-icon.png'

const auth = useAuthStore()

const orgName = computed(() => {
  const orgId = auth.organizationId
  const membership = auth.memberships.find((m) => m.organizationId === orgId)
  return membership?.organizationName ?? 'Organisasjon'
})

const orgNumber = computed(() => {
  const orgId = auth.organizationId
  const membership = auth.memberships.find((m) => m.organizationId === orgId)
  const num = membership?.orgNumber
  return num ? `Org. nr: ${num}` : ''
})

const isManagerOrAdmin = computed(() =>
  ['ADMIN', 'MANAGER'].includes(auth.role ?? ''),
)

const navMain = computed(() => [
  {
    label: 'OVERSIKT',
    items: [
      {title: 'Dashboard', url: '/', icon: LayoutDashboard},
    ],
  },
  {
    label: 'IK-MAT',
    items: [
      ...(isManagerOrAdmin.value
        ? [{title: 'HACCP-oppsett', url: '/haccp-oppsett', icon: ListChecks}]
        : []),
      {title: 'Sjekklister', url: '/sjekklister', icon: ClipboardCheck},
      {title: 'Temperaturlogg', url: '/temperatur', icon: Thermometer},
      ...(isManagerOrAdmin.value
        ? [{title: 'Hvitevarer', url: '/temperatur/hvitevarer', icon: Refrigerator}]
        : []),
    ],
  },
  {
    label: 'IK-ALKOHOL',
    items: [
      ...(isManagerOrAdmin.value
        ? [{title: 'Opplæring', url: '/opplaering', icon: GraduationCap}]
        : []),
      {title: 'Bevilling', url: '/bevilling', icon: ScrollText},
      ...(isManagerOrAdmin.value
        ? [{title: 'Skjenkepolicy', url: '/skjenkepolicy', icon: ShieldCheck}]
        : []),
    ],
  },
  {
    label: 'FELLES',
    items: [
      {title: 'Avvik', url: '/avvik', icon: AlertTriangle},
      ...(isManagerOrAdmin.value
        ? [
          {title: 'Rapporter', url: '/rapporter', icon: FileText},
          {title: 'Ansatte', url: '/ansatte', icon: Users},
        ]
        : []),
    ],
  },
])

const navSecondary = [
  {title: 'Innstillinger', url: '/innstillinger', icon: Settings},
]
</script>

<template>
  <Sidebar variant="inset" collapsible="icon">
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton size="lg" as="div">
            <div class="sidebar-brand-icon">
              <img :src="veraIcon" alt="Vera ikon" class="sidebar-brand-icon-image">
            </div>
            <div class="sidebar-brand-text">
              <span class="sidebar-brand-name">{{ orgName }}</span>
              <span class="sidebar-brand-sub">{{ orgNumber }}</span>
            </div>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>
    <SidebarContent>
      <NavMain :sections="navMain"/>
      <NavSecondary :items="navSecondary">
        <NavNotifications/>
      </NavSecondary>
    </SidebarContent>
    <SidebarSeparator/>
    <SidebarFooter>
      <NavUser/>
    </SidebarFooter>
  </Sidebar>
</template>

<style scoped>
.sidebar-brand-icon {
  display: flex;
  aspect-ratio: 1;
  width: 2rem;
  height: 2rem;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  background-color: hsl(var(--primary, 245 43% 52%));
  color: hsl(var(--primary-foreground, 0 0% 100%));
  font-weight: 700;
  font-size: 0.75rem;
  flex-shrink: 0;
}

.sidebar-brand-icon-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 0.4rem;
  background-color: white;
}

.sidebar-brand-text {
  display: grid;
  flex: 1;
  text-align: left;
  font-size: 0.875rem;
  line-height: 1.25;
  min-width: 0;
}

.sidebar-brand-name {
  font-weight: 600;
  color: hsl(var(--sidebar-foreground, 24 10% 10%));
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-brand-sub {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground, 24 10% 40%));
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
