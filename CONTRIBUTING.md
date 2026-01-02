# Contributing to Payment Processing System

Thank you for your interest in contributing! This document provides guidelines for contributing to this project.

## Getting Started

1. **Fork the repository**
2. **Clone your fork:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/payment-processing-system.git
   cd payment-processing-system
   ```
3. **Set up development environment** (see INSTALLATION.md)

## Development Workflow

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

### 2. Make Your Changes

- Follow Java coding conventions
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass: `mvn test`

### 3. Commit Your Changes

Use meaningful commit messages:

```bash
git commit -m "feat: add payment batch processing

- Implement batch payment API endpoint
- Add batch processing service
- Include unit tests"
```

**Commit Message Convention:**

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `refactor:` - Code refactoring
- `test:` - Adding tests
- `chore:` - Build/config changes

### 4. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub.

## Code Quality Standards

### Required Checks

- ✅ All tests must pass
- ✅ Code coverage should be maintained
- ✅ No compiler warnings
- ✅ Follow Spring Boot best practices

### Code Style

- Use Lombok for boilerplate reduction
- Add JavaDoc for public APIs
- Keep methods focused and small
- Use meaningful variable names

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Module Tests

```bash
mvn test -pl payment-core
```

### Integration Tests

```bash
mvn verify
```

## Pull Request Process

1. **Update documentation** if needed
2. **Ensure CI/CD passes** (GitHub Actions)
3. **Request review** from maintainers
4. **Address feedback** promptly
5. **Squash commits** if requested

## Questions?

Feel free to open an issue for:

- Bug reports
- Feature requests
- Questions about the codebase

---

**Thank you for contributing! 🎉**
