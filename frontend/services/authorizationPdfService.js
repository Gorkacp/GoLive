/**
 * Servicio para generar PDFs de autorización
 * Centraliza toda la lógica de generación de documentos PDF
 */

/**
 * Formatea una fecha al formato español
 * @param {string|Date} date - Fecha a formatear
 * @returns {string} Fecha formateada
 */
const formatDate = (date) => {
  if (!date) return ''
  try {
    const d = new Date(date)
    return new Intl.DateTimeFormat('es-ES', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(d)
  } catch {
    return date
  }
}

/**
 * Descarga un PDF de autorización para menores de edad
 * @param {Object} event - Objeto del evento con título, venue, fecha
 * @param {Object} options - Opciones adicionales
 */
export const downloadAuthorizationPDF = async (event, options = {}) => {
  try {
    const { jsPDF } = await import('jspdf')
    const doc = new jsPDF('p', 'mm', 'a4')
    const pageWidth = doc.internal.pageSize.getWidth()
    const pageHeight = doc.internal.pageSize.getHeight()
    
    let yPos = 15
    const lineHeight = 6
    const margin = 15
    const contentWidth = pageWidth - 2 * margin
    
    // Configuración de colores
    const colorText = [0, 0, 0]
    const colorGray = [100, 100, 100]
    const colorAccent = [255, 107, 53]
    
    // ============ HEADER ============
    doc.setTextColor(...colorText)
    doc.setFontSize(18)
    doc.setFont(undefined, 'bold')
    doc.text('AUTORIZACIÓN PARA ENTRADA A EVENTO', pageWidth / 2, yPos, { align: 'center' })
    yPos += 8
    
    doc.setFontSize(12)
    doc.setTextColor(...colorText)
    doc.text('Válida para menores de 18 años', pageWidth / 2, yPos, { align: 'center' })
    yPos += 10
    
    // ============ LÍNEA DIVISORA ============
    doc.setDrawColor(...colorText)
    doc.setLineWidth(1)
    doc.line(margin, yPos, pageWidth - margin, yPos)
    yPos += 8
    
    // ============ INFORMACIÓN DEL EVENTO ============
    doc.setTextColor(...colorText)
    doc.setFontSize(10)
    doc.setFont(undefined, 'normal')
    doc.text(`Evento: ${event.title || 'N/A'}`, margin, yPos)
    yPos += lineHeight
    
    doc.text(`Lugar: ${event.venue || 'N/A'}`, margin, yPos)
    yPos += lineHeight
    
    doc.text(`Fecha: ${formatDate(event.date)}`, margin, yPos)
    yPos += 8
    
    // ============ CAJA DE ADVERTENCIA ============
    doc.setTextColor(133, 100, 4)
    doc.setFillColor(255, 243, 205)
    doc.rect(margin, yPos, contentWidth, 12, 'F')
    doc.setFontSize(9)
    doc.text('IMPORTANTE: Este documento debe ser presentado impreso en la entrada del evento.', margin + 2, yPos + 5)
    doc.text('Es obligatorio para menores de 18 años.', margin + 2, yPos + 9)
    yPos += 15
    
    // ============ DATOS DEL MENOR ============
    doc.setTextColor(...colorText)
    doc.setFontSize(11)
    doc.setFont(undefined, 'bold')
    doc.text('DATOS DEL MENOR', margin, yPos)
    yPos += 8
    
    doc.setTextColor(...colorText)
    doc.setFontSize(9)
    doc.setFont(undefined, 'normal')
    doc.text('Nombre y Apellidos: _______________________________________________', margin, yPos)
    yPos += lineHeight
    
    doc.text('Fecha de Nacimiento: __________________  DNI/Pasaporte: ______________', margin, yPos)
    yPos += 8
    
    // ============ DATOS DEL RESPONSABLE ============
    doc.setTextColor(...colorText)
    doc.setFontSize(11)
    doc.setFont(undefined, 'bold')
    doc.text('DATOS DEL RESPONSABLE/TUTOR LEGAL', margin, yPos)
    yPos += 8
    
    doc.setTextColor(...colorText)
    doc.setFontSize(9)
    doc.setFont(undefined, 'normal')
    doc.text('Nombre y Apellidos: _______________________________________________', margin, yPos)
    yPos += lineHeight
    
    doc.text('Relación con el menor: ________________________  DNI/Pasaporte: __________', margin, yPos)
    yPos += lineHeight
    
    doc.text('Teléfono: _________________________', margin, yPos)
    yPos += 8
    
    // ============ AUTORIZACIÓN Y CONSENTIMIENTO ============
    doc.setTextColor(...colorText)
    doc.setFontSize(11)
    doc.setFont(undefined, 'bold')
    doc.text('AUTORIZACIÓN Y CONSENTIMIENTO', margin, yPos)
    yPos += 8
    
    doc.setTextColor(...colorText)
    doc.setFontSize(9)
    doc.setFont(undefined, 'normal')
    const authText = `Yo/Nosotros, en calidad de tutor/tutores legales del menor arriba mencionado, AUTORIZO/AUTORIZAMOS su asistencia al evento "${event.title}" que tendrá lugar el ${formatDate(event.date)}.`
    doc.text(authText, margin, yPos, { maxWidth: contentWidth, align: 'justify' })
    yPos += 18
    
    doc.text('Confirmo que he leído y acepto las condiciones del evento', margin, yPos)
    yPos += lineHeight
    
    doc.text('Autorizo el acceso del menor con esta documentación', margin, yPos)
    yPos += lineHeight
    
    doc.text('Confirmo que el menor tiene autorización para asistir', margin, yPos)
    yPos += 8
    
    // ============ FIRMA Y FECHA ============
    doc.setTextColor(...colorText)
    doc.setFontSize(11)
    doc.setFont(undefined, 'bold')
    doc.text('FIRMA Y FECHA', margin, yPos)
    yPos += 8
    
    doc.setTextColor(...colorText)
    doc.setFontSize(9)
    doc.setFont(undefined, 'normal')
    doc.text('Firma del Responsable: ______________________  Fecha: _______________', margin, yPos)
    yPos += 12
    
    // ============ FOOTER ============
    doc.setTextColor(...colorGray)
    doc.setFontSize(8)
    doc.text('Este documento fue generado desde GoLive | Gestor de Entradas', pageWidth / 2, pageHeight - 10, { align: 'center' })
    
    // ============ DESCARGAR ============
    const filename = `autorizacion-menores-${event.title.replace(/\s+/g, '-')}.pdf`
    doc.save(filename)
    
    return {
      success: true,
      message: `PDF descargado exitosamente: ${filename}`
    }
  } catch (error) {
    console.error('Error generando PDF de autorización:', error)
    return {
      success: false,
      error: error.message || 'Error al generar el PDF'
    }
  }
}

/**
 * Genera un PDF de ticket (para futuras implementaciones)
 * @param {Object} ticketData - Datos del ticket
 */
export const downloadTicketPDF = async (ticketData) => {
  try {
    const { jsPDF } = await import('jspdf')
    // Implementación futura
    console.log('Generando PDF de ticket...', ticketData)
  } catch (error) {
    console.error('Error generando PDF de ticket:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * Genera un PDF de confirmación de compra
 * @param {Object} orderData - Datos de la orden
 */
export const downloadOrderConfirmationPDF = async (orderData) => {
  try {
    const { jsPDF } = await import('jspdf')
    // Implementación futura
    console.log('Generando PDF de confirmación de compra...', orderData)
  } catch (error) {
    console.error('Error generando PDF de confirmación:', error)
    return {
      success: false,
      error: error.message
    }
  }
}
