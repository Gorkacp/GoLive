# 🎵 GoLive - Plataforma de Venta de Entradas para Eventos

Un proyecto Full Stack que nace de la necesidad de crear una solución completa y profesional para la gestión y venta de entradas para eventos musicales (conciertos y festivales).

## 🎯 ¿Qué hace el proyecto?

GoLive es una plataforma integral que permite a los organizadores de eventos gestionar, promocionar y vender entradas de forma eficiente, mientras ofrece a los usuarios una experiencia de compra fluida y segura. El sistema incluye:

- **Venta de entradas online** con múltiples zonas de precios
- **Panel de administración completo** con analytics en tiempo real
- **Sistema de pagos integrado** con PayPal
- **Generación automática de tickets** con códigos QR
- **Notificaciones push** para recordatorios de eventos
- **Chat asistente inteligente** para atención al cliente
- **Aplicación PWA** instalable en dispositivos móviles
- **Multiidioma** (Español, Inglés, Portugués)

## 🛠️ Stack Tecnológico

### Frontend
- **Nuxt.js 3** - Framework Vue.js con SSR y optimizaciones automáticas
- **Vue 3** - Framework JavaScript reactivo
- **Pinia** - Gestión de estado
- **Bootstrap 5** - Framework CSS para diseño responsivo
- **Bootstrap Icons & Font Awesome** - Iconografía
- **Chart.js** - Gráficos y visualización de datos
- **Leaflet** - Mapas interactivos
- **Vue I18n** - Internacionalización
- **Axios** - Cliente HTTP para peticiones API

### Backend
- **Spring Boot 3.2.7** - Framework Java para aplicaciones empresariales
- **Java 17** - Lenguaje de programación
- **Spring Security** - Autenticación y autorización
- **Spring Data MongoDB** - Integración con base de datos
- **JWT (JSON Web Tokens)** - Autenticación stateless
- **BCrypt** - Encriptación de contraseñas
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestión de dependencias

### Base de Datos
- **MongoDB** - Base de datos NoSQL
- **MongoDB Atlas** - Base de datos en la nube

### DevOps & Infraestructura
- **Docker** - Containerización
- **Docker Compose** - Orquestación de contenedores
- **Maven** - Build automation

### Librerías y Utilidades
- **ZXing** - Generación de códigos QR
- **Apache PDFBox** - Generación de PDFs
- **Web Push (VAPID)** - Notificaciones push para PWA
- **QRCode** - Generación de códigos QR en frontend
- **html2canvas & jsPDF** - Exportación de contenido a PDF

## 🔌 APIs y Servicios Externos Integrados

### PayPal API
- **Integración completa** para procesamiento de pagos
- **SDK de PayPal** para botones de pago
- **Captura de pagos** y gestión de transacciones
- **Validación de órdenes** y manejo de webhooks

### SendGrid API
- **Envío de emails transaccionales** mediante API REST
- **Emails de recuperación de contraseña**
- **Confirmación de compra** con tickets adjuntos
- **Notificaciones de eventos** y recordatorios

### Web Push (VAPID)
- **Notificaciones push nativas** para PWA
- **Protocolo VAPID** para autenticación
- **Suscripciones de usuarios** para notificaciones

### MongoDB Atlas
- **Base de datos en la nube** con alta disponibilidad
- **Conexión segura** mediante URI con autenticación

## ✨ Funcionalidades Principales

### Para Usuarios Finales
- 🔍 **Búsqueda avanzada** de eventos con autocompletado
- 🎫 **Compra de entradas** con selección de zonas y asistentes
- 💳 **Pago seguro** integrado con PayPal
- 📱 **Gestión de entradas** con códigos QR
- 🔔 **Notificaciones push** de recordatorios
- 💬 **Chat asistente** para consultas
- 🌍 **Multiidioma** (ES, EN, PT)
- 📱 **PWA instalable** en móviles

### Para Administradores
- 📊 **Dashboard analítico** con métricas en tiempo real
- 📈 **Gráficos de rendimiento** por categoría y evento
- 👥 **Gestión de usuarios** y roles (user, admin, super_user)
- 🎪 **CRUD completo de eventos** con zonas de precios
- 📧 **Sistema de notificaciones** masivas
- 💰 **Seguimiento de ingresos** y ocupación
- 📄 **Exportación de reportes**

### Características Técnicas
- 🔐 **Autenticación JWT** con refresh tokens
- 🛡️ **Autorización basada en roles** (RBAC)
- 🔄 **Validación de inventario** en tiempo real
- 📝 **Generación automática de tickets** con QR
- 📊 **Sistema de transacciones** completo
- 🗺️ **Integración de mapas** para ubicaciones
- 📱 **Diseño responsive** y mobile-first
- ⚡ **Optimizaciones de rendimiento** (lazy loading, caching)

## 📁 Estructura del Proyecto

```
GoLive/
├── frontend/                 # Aplicación Nuxt.js
│   ├── components/          # Componentes Vue reutilizables
│   ├── pages/               # Páginas y rutas
│   ├── composables/         # Lógica reutilizable (hooks)
│   ├── stores/              # Estado global (Pinia)
│   ├── plugins/             # Plugins de Nuxt
│   ├── middleware/          # Middleware de autenticación
│   ├── locales/             # Archivos de traducción
│   └── services/            # Servicios del frontend
│
├── backend/                 # Aplicación Spring Boot
│   ├── controller/          # Controladores REST
│   ├── services/            # Lógica de negocio
│   ├── repository/          # Acceso a datos (MongoDB)
│   ├── model/               # Entidades del dominio
│   ├── dto/                 # Data Transfer Objects
│   ├── config/              # Configuración (Security, etc.)
│   └── validation/          # Validadores personalizados
│
└── docker-compose.yml       # Configuración Docker
```

## 💡 ¿Qué aprendí desarrollándolo?

### Arquitectura y Diseño
- ✅ Cómo estructurar un proyecto **Full Stack** desde cero con separación clara de responsabilidades
- ✅ Implementación de **arquitectura REST** escalable y mantenible
- ✅ Diseño de **APIs RESTful** con Spring Boot siguiendo mejores prácticas
- ✅ Gestión de **estado global** con Pinia en aplicaciones Vue complejas
- ✅ Implementación de **middleware** y **guards** para protección de rutas

### Integraciones y APIs
- ✅ Integración de **pasarelas de pago** (PayPal SDK) con manejo de webhooks
- ✅ Implementación de **servicios de email** transaccionales (SendGrid API)
- ✅ Configuración de **notificaciones push** con protocolo VAPID para PWA
- ✅ Conexión con **bases de datos en la nube** (MongoDB Atlas)
- ✅ Integración de **servicios de analytics** (Firebase)

### Seguridad
- ✅ Implementación de **autenticación JWT** stateless
- ✅ **Autorización basada en roles** (RBAC) con Spring Security
- ✅ **Encriptación de contraseñas** con BCrypt
- ✅ Validación y sanitización de datos de entrada
- ✅ Protección de endpoints sensibles

### Frontend Avanzado
- ✅ Desarrollo de **Progressive Web App (PWA)** instalable
- ✅ Implementación de **internacionalización (i18n)** multiidioma
- ✅ Creación de **componentes reutilizables** y composables
- ✅ Optimización de **rendimiento** con lazy loading y code splitting
- ✅ Diseño **responsive** y mobile-first
- ✅ Integración de **gráficos y visualizaciones** (Chart.js)

### Backend Empresarial
- ✅ Desarrollo con **Spring Boot** siguiendo arquitectura en capas
- ✅ Implementación de **servicios de negocio** complejos
- ✅ Gestión de **transacciones** y consistencia de datos
- ✅ Manejo de **excepciones** centralizado
- ✅ **Logging** estructurado para debugging y monitoreo

### DevOps
- ✅ **Containerización** con Docker
- ✅ Orquestación con **Docker Compose**
- ✅ Configuración de **entornos** (desarrollo, producción)
- ✅ Gestión de **variables de entorno** y secretos

### Funcionalidades Específicas
- ✅ Generación de **códigos QR** para tickets
- ✅ Creación de **PDFs** con información de tickets
- ✅ Sistema de **inventario en tiempo real** con validación
- ✅ Implementación de **chat asistente** con procesamiento de lenguaje natural básico
- ✅ Dashboard con **analytics** y métricas de negocio

## 📊 Estadísticas del Proyecto

- **+42 endpoints REST** implementados
- **+15 componentes Vue** reutilizables
- **+10 servicios** de negocio en backend
- **3 idiomas** soportados (ES, EN, PT)
- **Arquitectura** frontend/backend completamente separada
- **PWA** con notificaciones push nativas

## 🔐 Seguridad Implementada

- Autenticación JWT con tokens seguros
- Encriptación de contraseñas con BCrypt
- Validación de datos en frontend y backend
- Protección CSRF (configurada en Spring Security)
- Autorización basada en roles
- Sanitización de inputs

## 📝 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 👤 Autor

**Gorka Carmona Pino**

---

⭐ Si te gusta este proyecto, ¡dale una estrella en GitHub!
