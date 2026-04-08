<script setup lang="ts">
import { inject, ref, onBeforeUnmount, watch, nextTick } from "vue"

const props = withDefaults(defineProps<{
  class?: string
  align?: "start" | "center" | "end"
  side?: "bottom" | "top" | "left" | "right"
  sideOffset?: number
}>(), {
  align: "start",
  side: "bottom",
  sideOffset: 4,
})

import type { Ref } from "vue"

const { isOpen, triggerRef, close } = inject("dropdown-menu") as {
  isOpen: Ref<boolean>
  triggerRef: Ref<HTMLElement | null>
  close: () => void
}
const contentRef = ref<HTMLElement | null>(null)
const posStyle = ref<Record<string, string>>({})
const VIEWPORT_PADDING = 8

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max)
}

function updatePosition() {
  const trigger = triggerRef.value
  if (!trigger) return

  const content = contentRef.value
  const rect = trigger.getBoundingClientRect()
  const s: Record<string, string> = { position: "fixed", zIndex: "999" }
  const menuWidth = content?.offsetWidth ?? 0
  const menuHeight = content?.offsetHeight ?? 0
  const isVertical = props.side === "bottom" || props.side === "top"

  if (isVertical) {
    let top = props.side === "bottom"
      ? rect.bottom + props.sideOffset
      : rect.top - props.sideOffset - menuHeight

    if (props.side === "bottom" && menuHeight > 0 && top + menuHeight + VIEWPORT_PADDING > window.innerHeight) {
      const flippedTop = rect.top - props.sideOffset - menuHeight
      top = flippedTop >= VIEWPORT_PADDING
        ? flippedTop
        : Math.max(VIEWPORT_PADDING, window.innerHeight - menuHeight - VIEWPORT_PADDING)
    }

    if (props.side === "top" && menuHeight > 0 && top < VIEWPORT_PADDING) {
      const flippedTop = rect.bottom + props.sideOffset
      top = flippedTop + menuHeight + VIEWPORT_PADDING <= window.innerHeight
        ? flippedTop
        : VIEWPORT_PADDING
    }

    s.top = `${Math.round(top)}px`

    let left = rect.left
    if (props.align === "start") {
      left = rect.left
    } else if (props.align === "center") {
      left = rect.left + (rect.width - menuWidth) / 2
    } else if (props.align === "end") {
      left = rect.right - menuWidth
    }

    if (menuWidth > 0) {
      left = clamp(left, VIEWPORT_PADDING, window.innerWidth - menuWidth - VIEWPORT_PADDING)
    }

    s.left = `${Math.round(left)}px`
  } else {
    let left = props.side === "right"
      ? rect.right + props.sideOffset
      : rect.left - props.sideOffset - menuWidth

    if (props.side === "right" && menuWidth > 0 && left + menuWidth + VIEWPORT_PADDING > window.innerWidth) {
      const flippedLeft = rect.left - props.sideOffset - menuWidth
      left = flippedLeft >= VIEWPORT_PADDING
        ? flippedLeft
        : Math.max(VIEWPORT_PADDING, window.innerWidth - menuWidth - VIEWPORT_PADDING)
    }

    if (props.side === "left" && menuWidth > 0 && left < VIEWPORT_PADDING) {
      const flippedLeft = rect.right + props.sideOffset
      left = flippedLeft + menuWidth + VIEWPORT_PADDING <= window.innerWidth
        ? flippedLeft
        : VIEWPORT_PADDING
    }

    s.left = `${Math.round(left)}px`

    let top = rect.top
    if (props.align === "start") {
      top = rect.top
    } else if (props.align === "center") {
      top = rect.top + (rect.height - menuHeight) / 2
    } else if (props.align === "end") {
      top = rect.bottom - menuHeight
    }

    if (menuHeight > 0) {
      top = clamp(top, VIEWPORT_PADDING, window.innerHeight - menuHeight - VIEWPORT_PADDING)
    }

    s.top = `${Math.round(top)}px`
  }

  posStyle.value = s
}

function onViewportChange() {
  if (isOpen.value) updatePosition()
}

function onClickOutside(e: MouseEvent) {
  if (!contentRef.value) return
  const trigger = triggerRef.value
  if (trigger && trigger.contains(e.target as Node)) return
  if (!contentRef.value.contains(e.target as Node)) {
    close()
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === "Escape") close()
}

watch(isOpen, async (open: boolean) => {
  if (open) {
    updatePosition()
    await nextTick()
    updatePosition()
    document.addEventListener("mousedown", onClickOutside)
    document.addEventListener("keydown", onKeydown)
    window.addEventListener("resize", onViewportChange)
    window.addEventListener("scroll", onViewportChange, true)
  } else {
    document.removeEventListener("mousedown", onClickOutside)
    document.removeEventListener("keydown", onKeydown)
    window.removeEventListener("resize", onViewportChange)
    window.removeEventListener("scroll", onViewportChange, true)
  }
})

onBeforeUnmount(() => {
  document.removeEventListener("mousedown", onClickOutside)
  document.removeEventListener("keydown", onKeydown)
  window.removeEventListener("resize", onViewportChange)
  window.removeEventListener("scroll", onViewportChange, true)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="dropdown">
      <div
        v-if="isOpen"
        ref="contentRef"
        :class="['dropdown-content', props.class]"
        :style="posStyle"
        role="menu"
      >
        <slot />
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dropdown-content {
  min-width: 14rem;
  overflow: hidden;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border, 35 15% 90%));
  background-color: hsl(var(--card, 40 25% 98%));
  color: hsl(var(--card-foreground, 24 10% 10%));
  padding: 0.25rem;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  outline: none;
}

/* Animation */
.dropdown-enter-active {
  animation: dropdown-in 150ms ease-out;
}

.dropdown-leave-active {
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
