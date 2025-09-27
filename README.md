# Nexa

Nexa provides a unified, context-aware, and self-evolving entry point for mobile services.  

NexaServer serves as the backend implementation of Nexa.  


## ✨ Features

- 🎯 **Unified Entry**: A unified entry point for all mobile services.  
- 🌍 **Context-Aware**: Supports multi-modal input, including user input (photos, voice, motion gestures, and circled screen content) and contextual data (time, location, activity, network status, etc.) to access mobile services.  
- 🌱 **Self-Evolving**: Learn from user phone usage to expand the service and rule databases for future service recommendation. 

## 🔧 Implementation
### Implemented Services
The system currently supports the following services:
- Snap-to-Shop  
- Add to Calendar  
- Set Alarm  
- Check Weather  
- Image Q&A  
- Online Search  
- Reply Assistant  
- Text Translation  
- App Search  

### Service Recommendation Module
The service recommendation module adopts a dual-process design:
- **System 1**: Performs fast, rule-based matching for immediate predictions.  
- **System 2**: Falls back to LLM-based reasoning when no predefined rule applies.  

Rules are represented in an **IF–THEN** format:  
- The **IF** part may consist of one or more predicates combined with logical AND (e.g., *screen content contains `<clothes>`*).  
- The **THEN** part specifies the target service, defined by its intent, associated slots, and the app name (e.g., *query_weather(location, date, app)*).

### Reflection Module (In Progress)
The reflection module, which is responsible for analyzing user interactions and generating new rules (self-evolving capability), is being integrated and should be completed soon.


## 📖 Examples

### Example 1: Image-Based Shopping
- **User**: Circles the shoulder bag on the screen  
- **System**: Recommends available services  
- **User**: Selects the image-based shopping service  
- **System**: Executes the service  

https://github.com/user-attachments/assets/7b0f0f9d-89c8-4292-b2da-fe560cb6ce32


### Example 2: Image Q&A
- **User**: Circles the cat on the screen and asks about its breed  
- **System**: Selects the chat service and replies with the cat’s breed  

https://github.com/user-attachments/assets/ee77258a-c617-4114-94bc-92c839f36dc8


### Example 3: Add to Calendar
- **User**: Activates the assistant from the home screen  
- **System**: Recommends the weather check service  
- **User**: Issues a natural language command to add a schedule  
- **System**: Asks for meeting location  
- **User**: Provides the location information  
- **System**: Executes the service  

https://github.com/user-attachments/assets/41807a90-1eea-4a68-9a4b-e4f6236d9ab1











