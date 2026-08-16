# Japanese Learning Platform (BrianJP)

Welcome to the **Japanese Learning Platform** (also known as BrianJP). This is a comprehensive online Japanese learning system meticulously designed to support students from N5 to N1 levels through various interactive learning methodologies, coupled with professional course management capabilities.

## 🌟 Vision & Objectives

The project aims to build a professional-grade Japanese learning platform. Rather than serving as a simple course marketplace, it is engineered as a complete and engaging learning ecosystem, featuring:
- **Multi-format Courses**: A rich blend of video, audio, text, and PDF materials, offering both free and premium tiers.
- **Interactive Learning**: Integrated Flashcards utilizing Spaced Repetition systems and comprehensive end-of-lesson Quizzes.
- **Gamification**: A rewarding progression system incorporating Experience Points (XP), learning Streaks, Leaderboards, and Badges to maximize user retention.
- **In-depth JLPT Content**: Extensive repositories of vocabulary, grammar, and Kanji, systematically categorized from N5 to N1 levels.
- **Seamless Payments**: Integrated with VNPAY and MOMO, tailored for convenient transactions in the Vietnamese market.
- **Admin Content Management System (CMS)**: A robust dashboard for managing courses, users, orders, system configurations, FAQs, and blogs.

## 🚀 Technology Stack

The platform is architected using a **Modular Monolith** approach. This ensures streamlined development and maintainability during initial phases while providing a solid foundation for future scalability.

### Frontend
- **Framework**: Vue 3, Vite
- **State Management**: Pinia
- **Routing**: Vue Router
- **HTTP Client**: Axios
- **Styling**: Tailwind CSS
- **UI Components**: Element Plus / Naive UI
- **Utilities**: VueUse

### Backend
- **Core**: Java 17+, Spring Boot
- **Security**: Spring Security, JWT (Access & Refresh Tokens)
- **Database Access**: Spring Data JPA, Hibernate
- **Validation**: Spring Validation
- **Documentation**: Swagger / OpenAPI

### Database & Infrastructure
- **Relational Database**: MariaDB
- **Caching**: Redis
- **File Storage**: Cloudflare R2 / AWS S3
- **Deployment & DevOps**: Docker, Docker Compose, Nginx, GitHub Actions

## 📂 Knowledge Base Structure

All standardization guidelines, feature specifications, Database schemas, and API designs are centrally managed within the `docs/` directory. This documentation system acts as the technical "brain" of the project, encompassing:

1. **Overview, Architecture & Technology**: Defines the project context, system design, and the directory structures for both Frontend and Backend.
2. **Feature & Database Specifications**: Detailed breakdowns of the 9 core modules (User, Course, Learning Progress, Quiz, Flashcard, Gamification, JLPT, Payment, Admin) alongside their relational database designs.
3. **API Specifications**: Standardized API response formats and comprehensive lists of available endpoints.
4. **Enums, Permissions & Error Codes**: Clear definitions of application states, Role-Based Access Control (RBAC) matrices, and standardized error codes (`PREFIX_00X`).
5. **Business Flows & MVP Scope**: Strategic launch plans for the Minimum Viable Product (MVP), heavily prioritizing the core learning experience.
6. **Technical Standards**: Strict security guidelines, performance optimization strategies (e.g., mitigating N+1 query issues), AI collaboration workflows, and rigorous code review conventions.

📌 **Note**: To understand the recommended sequence for reviewing the documentation, please refer to: `docs/templates/FODDER_DOCS_FLOW.md`.

## 📄 License
(To be updated)
