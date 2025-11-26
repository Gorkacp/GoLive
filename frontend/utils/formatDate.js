const localeMap = { es: 'es-ES', en: 'en-US', pt: 'pt-PT' }

const parseDate = (dateStr) => {
  if (!dateStr) return null
  const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr
  return isNaN(date.getTime()) ? null : date
}

export const formatDateISO = (dateStr, locale = 'es') => {
  const date = parseDate(dateStr)
  if (!date) return ''
  return date.toLocaleDateString(localeMap[locale] || 'es-ES', { 
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' 
  }).toUpperCase()
}

export const formatDateForInput = (dateStr) => {
  const date = parseDate(dateStr)
  if (!date) return ''
  const [y, m, d] = [date.getFullYear(), date.getMonth() + 1, date.getDate()]
  return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
}

export const formatDateTime = (dateStr, timeStr = '', locale = 'es') => {
  const date = formatDateISO(dateStr, locale)
  return timeStr ? `${date} - ${timeStr}` : date
}