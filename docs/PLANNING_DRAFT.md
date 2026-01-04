## DDD
### Step 1:
- Crunch knowledge with domain experts until the important rules become clear.
- Build a Ubiquitous Language so that conversations, code, test, and APIs use the same domain terms (no translation layer).
- Keep the model tightly bound to implementation: the code should express the domain, not bury it in services/controllers or database tables.

### Step 2:
- Identify the bounded contexts (the parts of the system responsible for a particular domain).
- Bounded Context: A clear boundary within which a specific domain model and its language are valid and consistent.
  - Inside the boundary,
    - Words have one meaning. Language does NOT leak across boundaries.
    - Models are consistent.
    - Code reflects that meaning. Boundaries are enforced in code.
  - Outside the boundary,
    - The same words may mean something different.
- Bounded Context (Eric Evans):
  - A Bounded Context:
    - Defines where a model applies
    - Protects the Ubiquitous Language
    - Is enforced by code boundaries, not just diagrams
- Bounded Context is:
  - A Semantic Boundary
  - A Language Boundary
  - A model consistency boundary
- Remember: 
  - Bounded Context protects meaning.
  - Meaning protects design.
  - Design protects change.
- Between bounded contexts, communication happens via IDs or events, not shared models

## Defining the Ubiquitous Language (core nouns for the bookshop service)
- Book
- Inventory
- Admin
- User
- AvailableCount
- Catalog
- Login
- Price

## Identifying the bounded context
- Catalog Context (responsible for managing and viewing books + availability)
  - Responsible for describing and exposing books that exist in the system.
  - Inside Catalog Context:
    - Book metadata
    - Author
    - Price
    - Availability (for browsing)
  - Outside Catalog Context:
    - Who bought the book
    - Who rented it
    - Payments
    - Penalties
    - Login mechanics
  - Catalog Context Ubiquitous Language:
    - Book: A catalog item that can be browsed.
    - Author: Descriptive metadata.
    - Price: Display price
    - Available: availableCount > 0
  - Code:
    - ```
      catalog/
        ├─ domain/
        │   ├─ Book
        │   ├─ BookId
        │   └─ BookRepository (interface)
        ├─ application/
        │   ├─ CreateBookUseCase
        │   └─ ListAvailableBooksUseCase
        └─ interfaces/
            └─ CatalogController
      ```
- [LATER] Inventory Context (responsible for managing inventory)
- [LATER] Identity/Access Context (responsible for managing users, authentication, roles)
- [LATER] Purchase/Rental Context (responsible for managing rentals, transactions, payments, due dates)
  - Rental cares about:
    - Due dates
    - Penalties
    - Renter
    - Rental status
  - A Book in Rental Context:
    - RentedBook:
      - rentalId
      - bookId
      - rentedAt
      - dueDate
      - renterId
      - status

## Some subdomains in bookshop app
- Managing books
- Authenticating users
- Selling or renting books
- Tracking inventory
- Payments (later)

## Architecture Structure:
Clean Architecture / DDD-friendly structure:
Packages:
- domain (pure business rules)
  - model (Entity/Value Objects)
  - service (domain services if needed)
  - repository (interfaces only)
- application (use cases)
  - usecase (e.g., CreateBook, ListBooks)
  - dto (request/response models at use-case boundary)
- infrastructure (adapters)
  - persistence (JPA entities + Spring Data repos)
  - security (Spring Security config)
- interfaces (delivery layer)
  - rest (controllers)
  - mapper (mapping between layers)

## Model the domain (first cut)
Entity: Book
- Identity: BookId
- Attributes:
  - name
  - author
  - price
  - availabeCount
- Behaviors:
  - increaseStock(n)
  - decreaseStock(n)
  - (future buy/rent)

Value Objects:
- Money (currency + amount) or start with BigDecimal and later refactor
- BookName, AuthorName (optional early; add when validation grows)

Domain invariants (rules):
- price >= 0
- availableCount >= 0
- name not blank
- author not blank

## Defining application use cases
Admin
- CreateBook
- UpdateBook
- AdjustInventory (optional now; useful later)
- ListBooksAdmin (includes zero stock)
User
- ListAvailableBooks
- GetBookDetails
Auth
- Login (or integrate Spring Security + JWT/session)

## Defining ports (interfaces) from the domain/application
Create repository interfaces for persistence layer
- BookRepository
  - save(Book)
  - findById(BookId)
  - findAll()
  - findAvailable()
These are ports. Infrastructure provides adapters.

## Implementing infrastructure adapters (JPA)
- JPA entity: BookJpaEntity
- Spring Data repository: SpringDataBookRepository
- Adapter: JpaBookRepositoryAdapter implements BookRepository

## Delivery layer (REST)
Endpoints (initial):
- POST /admin/books (admin only)
- GET /books (user logged-in, shows availalbe only)
- GET /admin/books (admin view all)

## Security
- Starting with Spring Security + in-memory users (ADMIN/USER) to enfore roles.
- Then move to JWT + DB users when ready.

## Testing Strategy
- Domain unit tests (no Spring)
  - BookTest for invariants and behaviors
- Application tests (mock repositories)
  - CreateBookUseCaseTest
- Controller tests (MockMvc)
- Persistence tests (DataJpaTest)

