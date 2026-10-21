# ToLet Hub - Rent & Buy Homes (Java Swing)

A complete desktop real-estate platform written in pure Java (no external libraries, no database server).

## Quick start (easiest)
1. Install a JDK (version 11 or newer, 17/21 recommended): https://adoptium.net
2. **Windows:** double-click `run.bat`
   **Mac/Linux:** run `./run.sh`

### Command prompt (manual)
    cd ToLetHub
    mkdir out
    dir /s /b src\*.java > sources.txt          (Windows)
    javac -d out @sources.txt
    java -cp out com.tolet.Main

### VS Code
Install the "Extension Pack for Java", open the ToLetHub folder, open `src/com/tolet/Main.java`
and click **Run** above `main`.

## Demo accounts
| Role     | Email             | Password |
|----------|-------------------|----------|
| Customer | user@tolet.com    | user123  |
| Owner    | owner@tolet.com   | owner123 |
| Admin    | admin@tolet.com   | admin123 |

You can also register new Customer or Owner accounts.

## Features
- Login / registration with SHA-256 hashed passwords, blocked-user support
- Three roles: Customer, Owner, Admin
- Browse with search, filters (rent/sale, type, city, beds, price, furnished) and 7 sort modes
- Smart Match: % match score learned from your favorites and viewing history
- Illustrated property "photos" generated in code
- Property details: specs, amenities, owner contact, reviews and star ratings
- Loan/EMI sliders for sale homes, move-in cost estimate for rentals
- Favorites, recently viewed, side-by-side comparison of up to 3 homes
- Schedule site visits, send rent / buy requests with your own offer
- Owner: add/edit/delete listings, feature a listing, mark rented/sold, approve or decline requests
- Admin: approve listings, block users, platform report, CSV export, reset demo data
- Auto-generated rent agreement / sale confirmation (text file)
- Notification centre, dashboard charts, Smart Calculators (EMI, Rent vs Buy, Affordability)
- Light and dark mode
- All data saved automatically in `data/tolet.dat` (delete it to start fresh)

## Project structure
    src/com/tolet/Main.java        entry point
    src/com/tolet/model/           User, Property, Booking, Review, Notification
    src/com/tolet/data/            DataStore (persistence + demo data), Recommender
    src/com/tolet/ui/              all screens and custom widgets
