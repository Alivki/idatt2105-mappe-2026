<script setup lang="ts">
import {ref} from 'vue'
import {z} from 'zod/v4'
import {Mail} from 'lucide-vue-next'
import Button from '@/components/ui/button/Button.vue'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'

defineProps<{
  open: boolean
  isPending: boolean
}>()

const emits = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'invite', payload: { email: string; role: string }): void
}>()

const inviteEmail = ref('')
const inviteRole = ref('EMPLOYEE')
const inviteError = ref('')

const emailSchema = z.email({message: 'Ugyldig e-postadresse'})

function handleSubmit() {
  const result = emailSchema.safeParse(inviteEmail.value.trim())
  if (!result.success) {
    inviteError.value = result.error?.issues?.[0]?.message ?? 'Ugyldig e-postadresse'
    return
  }
  inviteError.value = ''
  emits('invite', {email: inviteEmail.value.trim(), role: inviteRole.value})
}

function handleOpen(v: boolean) {
  if (v) {
    inviteEmail.value = ''
    inviteRole.value = 'EMPLOYEE'
    inviteError.value = ''
  }
  emits('update:open', v)
}
</script>

<template>
  <Dialog :open="open" @update:open="handleOpen">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Legg til ansatt</DialogTitle>
        <DialogDescription>
          Inviter en ny ansatt til organisasjonen via e-post.
        </DialogDescription>
      </DialogHeader>

      <form class="invite-form" @submit.prevent="handleSubmit">
        <div class="form-field">
          <label class="form-label" for="invite-email">E-post</label>
          <div :class="['email-input-wrapper', { 'email-input-wrapper--error': inviteError }]">
            <Mail :size="16" class="email-input-icon" aria-hidden="true"/>
            <input
              id="invite-email"
              v-model="inviteEmail"
              type="email"
              class="email-input"
              placeholder="navn@eksempel.no"
              autocomplete="off"
            />
          </div>
          <span v-if="inviteError" class="form-error" role="alert">{{ inviteError }}</span>
        </div>

        <div class="form-field">
          <label class="form-label">Rolle</label>
          <Select v-model="inviteRole">
            <SelectTrigger>
              <SelectValue placeholder="Velg rolle"/>
            </SelectTrigger>
            <SelectContent open-upward>
              <SelectItem value="ADMIN">Admin</SelectItem>
              <SelectItem value="MANAGER">Leder</SelectItem>
              <SelectItem value="EMPLOYEE">Ansatt</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <DialogFooter>
          <Button variant="outline" type="button" @click="emits('update:open', false)">Avbryt
          </Button>
          <Button type="submit" :disabled="isPending">
            {{ isPending ? 'Sender...' : 'Send invitasjon' }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>

<style scoped>
.invite-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-label {
  font-size: 0.85rem;
  font-weight: 500;
  color: hsl(var(--foreground));
}

.email-input-wrapper {
  position: relative;
}

.email-input-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: hsl(var(--muted-foreground));
  pointer-events: none;
}

.email-input {
  display: flex;
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--input));
  background-color: hsl(var(--card));
  padding: 0.5rem 0.75rem 0.5rem 2.25rem;
  font-size: 0.875rem;
  line-height: 1.25rem;
  color: inherit;
  box-shadow: 0 1px 2px 0 hsl(var(--foreground) / 0.05);
  outline: none;
  transition: border-color 150ms ease, box-shadow 150ms ease;
  font-family: inherit;
}

.email-input::placeholder {
  color: hsl(var(--muted-foreground));
}

.email-input:focus {
  box-shadow: 0 0 0 2px hsl(var(--ring) / 0.2);
  border-color: hsl(var(--primary) / 0.5);
}

.form-error {
  font-size: 0.8rem;
  color: hsl(var(--destructive));
}

.email-input-wrapper--error .email-input {
  border-color: hsl(var(--destructive));
}
</style>
