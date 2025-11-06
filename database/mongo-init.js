// mongo-init.js
// -------------------
// Inicialización profesional de MongoDB para plataforma de venta de entradas
// -------------------

db = db.getSiblingDB('GoLive');

// -------------------
// Colección: users
// -------------------
db.createCollection('users', {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["email", "name", "role", "purchases"],
      properties: {
        email: { bsonType: "string", description: "Correo electrónico único" },
        name: { bsonType: "string", description: "Nombre completo del usuario" },
        role: { bsonType: "string", enum: ["admin", "user"], description: "Rol del usuario" },
        purchases: {
          bsonType: "array",
          description: "Historial de compras",
          items: { bsonType: "object" }
        }
      }
    }
  }
});
db.users.createIndex({ email: 1 }, { unique: true });
db.users.insertOne({
  email: "admin@example.com",
  name: "Admin",
  role: "admin",
  purchases: []
});

// -------------------
// Colección: events
// -------------------
db.createCollection('events', {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["eventId", "title", "category", "date", "time", "venue", "location", "image", "availableTickets", "zones"],
      properties: {
        eventId: { bsonType: "string" },
        title: { bsonType: "string" },
        category: { bsonType: "string" },
        date: { bsonType: "date" },
        time: { bsonType: "string" },
        venue: { bsonType: "string" },
        location: { bsonType: "string" },
        image: { bsonType: "string" },
        availableTickets: { bsonType: "int" },
        zones: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["name", "availableTickets", "price"],
            properties: {
              name: { bsonType: "string" },
              availableTickets: { bsonType: "int" },
              price: { bsonType: "double" }
            }
          }
        }
      }
    }
  }
});
db.events.createIndex({ eventId: 1 }, { unique: true });
db.events.insertOne({
  eventId: "event1",
  title: "ANUEL AA EUROPE TOUR 2025",
  category: "concierto",
  date: new Date("2025-10-25T23:00:00Z"),
  time: "23:00",
  venue: "Granada",
  location: "Plaza de Toros, Granada",
  image: "https://d31tcnbxvxtafg.cloudfront.net/images/events/e6df77a67909e7be1b62c51ed7eace51.png",
  availableTickets: 500,
  zones: [
    { name: "General", availableTickets: 300, price: 35.0 },
    { name: "Entrada Early Pass", availableTickets: 200, price: 50.0 },
    { name: "Entrada VIP", availableTickets: 100, price: 78.0 }
  ]
});

// -------------------
// Colección: tickets
// -------------------
db.createCollection('tickets', {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["ticketId", "eventId", "userId", "status", "price", "date"],
      properties: {
        ticketId: { bsonType: "string" },
        eventId: { bsonType: "string" },
        userId: { bsonType: "string" },
        status: { bsonType: "string", enum: ["available", "sold"] },
        price: { bsonType: "double" },
        date: { bsonType: "date" }
      }
    }
  }
});
db.tickets.createIndex({ ticketId: 1 }, { unique: true });
db.tickets.createIndex({ userId: 1 });
db.tickets.createIndex({ eventId: 1 });

// -------------------
// Colección: transactions (opcional, para historial de pagos)
// -------------------
db.createCollection('transactions', {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["transactionId", "userId", "ticketId", "amount", "date", "status"],
      properties: {
        transactionId: { bsonType: "string" },
        userId: { bsonType: "string" },
        ticketId: { bsonType: "string" },
        amount: { bsonType: "double" },
        date: { bsonType: "date" },
        status: { bsonType: "string", enum: ["pending", "completed", "failed"] }
      }
    }
  }
});
db.transactions.createIndex({ transactionId: 1 }, { unique: true });
db.transactions.createIndex({ userId: 1 });
db.transactions.createIndex({ ticketId: 1 });

// -------------------
// Colección: logs (opcional, para auditoría y seguimiento)
// -------------------
db.createCollection('logs', {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["action", "userId", "date", "details"],
      properties: {
        action: { bsonType: "string" },
        userId: { bsonType: "string" },
        date: { bsonType: "date" },
        details: { bsonType: "object" }
      }
    }
  }
});
db.logs.createIndex({ userId: 1 });
db.logs.createIndex({ date: -1 });

print("✅ Base de datos golive inicializada correctamente.");
