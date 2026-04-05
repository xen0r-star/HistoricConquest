<a id="readme-top"></a>

<h1 align="center">HistoricConquest</h1>

<p align="center">
  <img src="https://img.shields.io/github/license/xen0r-star/HistoricConquest?style=flat-square" alt="LICENSE"/>
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21-0A7BBB?style=flat-square" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=flat-square&logo=spring" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/WebSocket-STOMP-000000?style=flat-square" alt="WebSocket STOMP"/>
</p>

HistoricConquest is a strategy game of global conquest where knowledge is your main weapon.
Players answer themed questions with a self-assessed difficulty level (1 to 4) to expand territories,
move armies, and challenge opponents.

<details>
  <summary>🗂️ Table of Contents</summary>
  <ol>
    <li><a href="#features">✨ Features</a></li>
    <li><a href="#screenshots">🎮 Screenshots</a></li>
    <li><a href="#tech-stack">🛠️ Tech Stack</a></li>
    <li><a href="#project-architecture">🏗️ Project Architecture</a></li>
    <li><a href="#getting-started">🚀 Getting Started</a></li>
    <li><a href="#network-overview">🌐 Network Overview</a></li>
    <li><a href="#project-structure">📁 Project Structure</a></li>
    <li><a href="#contributing">🤝 Contributing</a></li>
    <li><a href="#license">📝 License</a></li>
    <li><a href="#authors">👤 Authors</a></li>
  </ol>
</details>

---

<a id="features"></a>
## ✨ Features

- Turn-based conquest gameplay with map movement and territorial control.
- Quiz-driven progression with multiple themes (History, Informatic, Tourism, Entertainment).
- Difficulty self-assessment system from 1 to 4 before answering questions.
- JavaFX desktop interface with FXML views, HUD, settings, notifications, and help overlay.
- Multiplayer lobby flow (create room, join room, bots, player updates, kick/quit events).
- Real-time messaging via WebSocket/STOMP between the JavaFX client and Spring Boot server.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="screenshots"></a>
### 🎮 Screenshots
<table>
  <tr>
    <td>
      <img src="./docs/screenshot1.png" width="500"/>
    </td>
    <td>
      <img src="./docs/screenshot2.png" width="500"/>
    </td>
  </tr>
  <tr>
    <td>
      <img src="./docs/screenshot3.png" width="500"/>
    </td>
    <td>
      <img src="./docs/screenshot4.png" width="500"/>
    </td>
  </tr>
</table>


<a id="tech-stack"></a>
## 🛠️ Tech Stack

- **Client**: Java 21, JavaFX 21, Jackson, Java-WebSocket, Logback
- **Server**: Spring Boot 4.0.3, Spring WebSocket, JWT (jjwt)
- **Build**: Maven (with Maven Wrapper)
- **Tests**: JUnit 5

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="project-architecture"></a>
## 🏗️ Project Architecture

- `src/` contains the JavaFX desktop application (`com.historicconquest.historicconquest`).
- `server/` contains the Spring Boot backend (`com.historicconquest.server`).
- Client and server are built independently with their own `pom.xml` files.
- Default local communication uses:
  - HTTP API: `http://localhost:8080/api`
  - WebSocket endpoint: `ws://localhost:8080/ws`

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="getting-started"></a>
## 🚀 Getting Started

### Prerequisites

- JDK 21
- Maven 3.9+ (optional if using wrapper)
- Git

### 1) Clone the repository

```bash
git clone https://github.com/xen0r-star/HistoricConquest.git
cd HistoricConquest
```

### 2) Start the server

From `server/`:

```bash
./mvnw spring-boot:run
```

Windows PowerShell alternative:

```powershell
.\mvnw.cmd spring-boot:run
```

### 3) Start the JavaFX client

The client entry point is `com.historicconquest.historicconquest.app.Launcher`.
You can run it directly from your IDE after the server is started.

To only build the client module from project root:

```bash
./mvnw clean package
```

Windows PowerShell alternative:

```powershell
.\mvnw.cmd clean package
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="network-overview"></a>
## 🌐 Network Overview

### HTTP endpoints

- `GET /api/health`: server health check.
- `POST /api/gameroom/create?playerName=...`: create a room and host token.
- `POST /api/gameroom/join?roomCode=...`: join an existing room.

### STOMP destinations

- Endpoint: `/ws`
- App prefix: `/app`
- Broker destinations: `/topic`, `/queue`

Examples used by the server include `/app/ping`, `/app/update`, `/app/addBot`, `/app/quit`.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="project-structure"></a>
## 📁 Project Structure

```text
HistoricConquest/
|- src/                          # JavaFX client
|  |- main/java/
|  |- main/resources/
|- server/                       # Spring Boot server
|  |- src/main/java/
|  |- src/main/resources/
|- pom.xml                       # Client/build config at root
|- README.md
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="contributing"></a>
## 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m "Add your feature"`)
4. Push the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="license"></a>
## 📝 License

This project is licensed under the MIT License.
See [`LICENSE`](LICENSE) for details.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<a id="authors"></a>
## 👤 Authors

- [xen0r-star](https://github.com/xen0r-star)
- [Ambhd](https://github.com/Ambhd)
- [ProgMaster17](https://github.com/ProgMaster17)
- [Aziz-Senna](https://github.com/Aziz-Senna)

<p align="right">(<a href="#readme-top">back to top</a>)</p>
