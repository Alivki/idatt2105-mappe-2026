<script setup lang="ts">
import { BellDot, CheckCheck } from 'lucide-vue-next'
import { ref, computed, watch, onBeforeUnmount, nextTick } from 'vue'
import {
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'
import {
  useUnreadCountQuery,
  useMarkAsReadMutation,
  useMarkAllAsReadMutation,
} from '@/composables/useNotifications'
import api from '@/lib/api'
import type { Notification, NotificationPage } from '@/types/notification'

const { data: unreadData } = useUnreadCountQuery()
const { mutate: markAsRead } = useMarkAsReadMutation()
const { mutate: markAllAsRead } = useMarkAllAsReadMutation()

const hasUnread = computed(() => (unreadData.value?.count ?? 0) > 0)

const isOpen = ref(false)
const triggerRef = ref<HTMLElement | null>(null)
const panelRef = ref<HTMLElement | null>(null)
const panelStyle = ref<Record<string, string>>({})

const notifications = ref<Notification[]>([])
const currentPage = ref(0)
const isLastPage = ref(false)
const loading = ref(false)

let closeTimer: ReturnType<typeof setTimeout> | null = null

function startCloseTimer() {
  stopCloseTimer()
  closeTimer = setTimeout(() => {
    isOpen.value = false
  }, 2000)
}

function stopCloseTimer() {
  if (closeTimer) {
    clearTimeout(closeTimer)
    closeTimer = null
  }
}

async function fetchPage(page: number) {
  if (loading.value) return
  loading.value = true
  try {
    const res = await api.get<NotificationPage>('/notifications', {
      params: { page, size: 20 },
    })
    const data = res.data
    if (page === 0) {
      notifications.value = data.content
    } else {
      notifications.value = [...notifications.value, ...data.content]
    }
    isLastPage.value = data.last
    currentPage.value = data.number
  } finally {
    loading.value = false
  }
}

function updatePosition() {
  const trigger = triggerRef.value
  if (!trigger) return
  const rect = trigger.getBoundingClientRect()
  const panelWidth = 352 // 22rem
  const isMobile = window.innerWidth < 640

  if (isMobile) {
    // On mobile, position above the trigger, centered horizontally
    panelStyle.value = {
      position: 'fixed',
      zIndex: '999',
      left: '0.5rem',
      right: '0.5rem',
      width: 'auto',
      bottom: `${window.innerHeight - rect.top + 8}px`,
    }
  } else if (rect.right + 4 + panelWidth > window.innerWidth) {
    // Not enough space to the right, open to the left
    panelStyle.value = {
      position: 'fixed',
      zIndex: '999',
      right: `${window.innerWidth - rect.left + 4}px`,
      bottom: `${window.innerHeight - rect.bottom}px`,
    }
  } else {
    panelStyle.value = {
      position: 'fixed',
      zIndex: '999',
      left: `${rect.right + 4}px`,
      bottom: `${window.innerHeight - rect.bottom}px`,
    }
  }
}

function toggle() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    currentPage.value = 0
    notifications.value = []
    fetchPage(0)
    updatePosition()
    startCloseTimer()
  }
}

function onClickOutside(e: MouseEvent) {
  if (triggerRef.value?.contains(e.target as Node)) return
  if (panelRef.value?.contains(e.target as Node)) return
  isOpen.value = false
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') isOpen.value = false
}

watch(isOpen, async (open) => {
  if (open) {
    await nextTick()
    document.addEventListener('mousedown', onClickOutside)
    document.addEventListener('keydown', onKeydown)
  } else {
    document.removeEventListener('mousedown', onClickOutside)
    document.removeEventListener('keydown', onKeydown)
    stopCloseTimer()
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onClickOutside)
  document.removeEventListener('keydown', onKeydown)
  stopCloseTimer()
})

function onPanelMouseEnter() {
  stopCloseTimer()
}

function onPanelMouseLeave() {
  startCloseTimer()
}

function onTriggerMouseEnter() {
  stopCloseTimer()
}

function onTriggerMouseLeave() {
  if (isOpen.value) startCloseTimer()
}

function handleScroll(e: Event) {
  const el = e.target as HTMLElement
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 10) {
    if (!isLastPage.value && !loading.value) {
      fetchPage(currentPage.value + 1)
    }
  }
}

function handleNotificationHover(notification: Notification) {
  if (!notification.isRead) {
    markAsRead(notification.id)
    notification.isRead = true
  }
}

function handleMarkAllAsRead() {
  markAllAsRead()
  notifications.value.forEach((n) => (n.isRead = true))
}

function formatTime(iso: string): string {
  const date = new Date(iso)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return 'Akkurat nå'
  if (diffMin < 60) return `${diffMin}m siden`
  const diffHours = Math.floor(diffMin / 60)
  if (diffHours < 24) return `${diffHours}t siden`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays}d siden`
  return date.toLocaleDateString('nb-NO', { day: 'numeric', month: 'short' })
}

function typeLabel(type: string): string {
  const labels: Record<string, string> = {
    BEVILLING_EXPIRY: 'Bevilling',
    TRAINING_EXPIRY: 'Opplæring',
    DEVIATION_CREATED: 'Avvik',
    DEVIATION_ASSIGNED: 'Avvik tildelt',
    CHECKLIST_OVERDUE: 'Sjekkliste',
    LOG_REMINDER: 'Logg',
    SYSTEM_ALERT: 'System',
  }
  return labels[type] ?? type
}
</script>

<template>
    <SidebarMenuItem>
      <div
        ref="triggerRef"
        @mouseenter="onTriggerMouseEnter"
        @mouseleave="onTriggerMouseLeave"
      >
        <SidebarMenuButton
          size="sm"
          tooltip="Varslingssenter"
          @click="toggle"
        >
          <div class="bell-wrapper">
            <BellDot />
            <span v-if="hasUnread" class="pulse-dot">
              <span class="pulse-dot-ping"></span>
              <span class="pulse-dot-core"></span>
            </span>
          </div>
          <span>Varslingssenter</span>
        </SidebarMenuButton>
      </div>

      <Teleport to="body">
        <Transition name="notif-dropdown">
          <div
            v-if="isOpen"
            ref="panelRef"
            class="notif-panel"
            :style="panelStyle"
            @mouseenter="onPanelMouseEnter"
            @mouseleave="onPanelMouseLeave"
          >
            <div class="notif-header">
              <span class="notif-header-title">Varslinger</span>
              <button
                v-if="hasUnread"
                class="notif-mark-all"
                @click="handleMarkAllAsRead"
              >
                <CheckCheck />
                <span>Merk alle som lest</span>
              </button>
            </div>

            <div class="notif-separator"></div>

            <div class="notif-list" @scroll="handleScroll">
              <div
                v-if="notifications.length === 0 && !loading"
                class="notif-empty"
              >
                Ingen varslinger
              </div>

              <div
                v-for="notif in notifications"
                :key="notif.id"
                class="notif-item"
                :class="{ 'notif-item--unread': !notif.isRead }"
                @mouseenter="handleNotificationHover(notif)"
              >
                <div class="notif-item-header">
                  <span class="notif-item-type">{{ typeLabel(notif.type) }}</span>
                  <div class="notif-item-right">
                    <span class="notif-item-time">{{ formatTime(notif.createdAt) }}</span>
                    <span v-if="!notif.isRead" class="pulse-dot pulse-dot--sm">
                      <span class="pulse-dot-ping"></span>
                      <span class="pulse-dot-core"></span>
                    </span>
                  </div>
                </div>
                <div class="notif-item-title">{{ notif.title }}</div>
                <div class="notif-item-message">{{ notif.message }}</div>
              </div>

              <div v-if="loading" class="notif-loading">Laster...</div>
            </div>
          </div>
        </Transition>
      </Teleport>
    </SidebarMenuItem>
</template>

<style scoped>
.bell-wrapper {
  position: relative;
  display: inline-flex;
}

.pulse-dot {
  position: absolute;
  top: -2px;
  right: -4px;
  display: flex;
  width: 0.625rem;
  height: 0.625rem;
}

.pulse-dot--sm {
  position: relative;
  top: auto;
  right: auto;
  width: 0.5rem;
  height: 0.5rem;
  flex-shrink: 0;
}

.pulse-dot-ping {
  position: absolute;
  display: inline-flex;
  height: 100%;
  width: 100%;
  border-radius: 9999px;
  background-color: hsl(0 72% 51%);
  opacity: 0.75;
  animation: ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite;
}

.pulse-dot-core {
  position: relative;
  display: inline-flex;
  width: 100%;
  height: 100%;
  border-radius: 9999px;
  background-color: hsl(0 72% 51%);
}

@keyframes ping {
  75%, 100% {
    transform: scale(2);
    opacity: 0;
  }
}

/* Panel */
.notif-panel {
  width: 22rem;
  max-height: 28rem;
  display: flex;
  flex-direction: column;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border, 35 15% 90%));
  background-color: hsl(var(--card, 40 25% 98%));
  color: hsl(var(--card-foreground, 24 10% 10%));
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

.notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
}

.notif-header-title {
  font-weight: 600;
  font-size: 0.875rem;
}

.notif-mark-all {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.75rem;
  color: hsl(var(--primary, 245 43% 52%));
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  transition: background-color 150ms ease;
}

.notif-mark-all:hover {
  background-color: hsl(var(--accent, 250 40% 95%));
}

.notif-mark-all :deep(svg) {
  width: 0.875rem;
  height: 0.875rem;
}

.notif-separator {
  height: 1px;
  background-color: hsl(var(--border, 35 15% 90%));
}

.notif-list {
  flex: 1;
  overflow-y: auto;
  max-height: 24rem;
}

.notif-empty {
  padding: 2rem 1rem;
  text-align: center;
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground, 24 10% 40%));
}

.notif-loading {
  padding: 0.75rem 1rem;
  text-align: center;
  font-size: 0.8125rem;
  color: hsl(var(--muted-foreground, 24 10% 40%));
}

/* Notification item */
.notif-item {
  padding: 0.625rem 1rem;
  border-bottom: 1px solid hsl(var(--border, 35 15% 90%));
  transition: background-color 150ms ease;
  cursor: default;
}

.notif-item:last-child {
  border-bottom: none;
}

.notif-item:hover {
  background-color: hsl(var(--accent, 250 40% 95%));
}

.notif-item--unread {
  background-color: hsl(var(--accent, 250 40% 95%) / 0.5);
}

.notif-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.125rem;
}

.notif-item-type {
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: hsl(var(--primary, 245 43% 52%));
}

.notif-item-right {
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.notif-item-time {
  font-size: 0.6875rem;
  color: hsl(var(--muted-foreground, 24 10% 40%));
}

.notif-item-title {
  font-size: 0.8125rem;
  font-weight: 600;
  line-height: 1.3;
  margin-bottom: 0.125rem;
}

.notif-item-message {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground, 24 10% 40%));
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@media (max-width: 639px) {
  .notif-panel {
    width: auto;
    max-height: 60vh;
  }
}

/* Animation */
.notif-dropdown-enter-active {
  animation: dropdown-in 150ms ease-out;
}

.notif-dropdown-leave-active {
  animation: dropdown-out 100ms ease-in;
}

@keyframes dropdown-in {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes dropdown-out {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.95);
  }
}
</style>
