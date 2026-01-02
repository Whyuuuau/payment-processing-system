# GitHub Setup Guide

Panduan lengkap untuk setup Git repository dan push ke GitHub.

## 📋 Prerequisites

1. **Install Git for Windows**

   ```bash
   # Download dari: https://git-scm.com/download/win
   # Atau menggunakan winget:
   winget install Git.Git
   ```

2. **Verify Installation**

   ```bash
   git --version
   # Output: git version 2.x.x
   ```

3. **Create GitHub Account**
   - Buka https://github.com dan daftar jika belum punya account

---

## 🚀 Quick Setup (Automatic)

### Option 1: Menggunakan Script

Jalankan script yang sudah disediakan:

```bash
git-setup.bat
```

Script ini akan:

- ✅ Initialize Git repository
- ✅ Configure Git user (jika belum)
- ✅ Add semua files
- ✅ Create initial commit
- ✅ Berikan instruksi untuk push ke GitHub

---

## 🔧 Manual Setup (Step by Step)

### 1. Configure Git User

Jika belum pernah setup Git sebelumnya:

```bash
# Set nama Anda
git config --global user.name "Nama Anda"

# Set email Anda (gunakan email GitHub)
git config --global user.email "email@example.com"
```

### 2. Initialize Repository

```bash
# Dari directory project
cd "e:\METRODATA\Learning\Try 1"

# Initialize git
git init
```

### 3. Add Files

```bash
# Add semua files
git add .

# Verify files yang akan di-commit
git status
```

### 4. Create Initial Commit

```bash
git commit -m "Initial commit: Payment Processing System

- Multi-module Maven project
- Spring Boot 3.2 with Java 21
- Resilience patterns implemented
- Complete documentation"
```

---

## 📤 Push to GitHub

### 1. Create New Repository di GitHub

1. Buka https://github.com/new
2. Repository name: `payment-processing-system`
3. Description: "Enterprise Payment Processing System with Spring Boot"
4. **Jangan** centang "Initialize with README" (sudah ada README.md)
5. Click "Create repository"

### 2. Add Remote & Push

GitHub akan memberikan commands, gunakan yang HTTPS:

```bash
# Add remote repository
git remote add origin https://github.com/YOUR_USERNAME/payment-processing-system.git

# Rename branch to main (if needed)
git branch -M main

# Push to GitHub
git push -u origin main
```

**Untuk SSH (jika sudah setup SSH key):**

```bash
git remote add origin git@github.com:YOUR_USERNAME/payment-processing-system.git
git branch -M main
git push -u origin main
```

---

## 🔐 Authentication

### Option 1: HTTPS dengan Personal Access Token (Recommended)

1. **Create Personal Access Token:**

   - Buka: https://github.com/settings/tokens
   - Click "Generate new token (classic)"
   - Pilih scope: `repo` (full control of private repositories)
   - Generate token dan **copy** (hanya ditampilkan sekali!)

2. **Saat Push dengan HTTPS:**

   ```bash
   git push -u origin main
   # Username: YOUR_GITHUB_USERNAME
   # Password: PASTE_YOUR_TOKEN (bukan password GitHub)
   ```

3. **Simpan Credentials (Optional):**
   ```bash
   # Windows Credential Manager akan menyimpan otomatis
   # Atau gunakan:
   git config --global credential.helper wincred
   ```

### Option 2: SSH Key

1. **Generate SSH Key:**

   ```bash
   ssh-keygen -t ed25519 -C "your_email@example.com"
   # Press Enter untuk default location
   # Optional: masukkan passphrase
   ```

2. **Add SSH Key to GitHub:**

   ```bash
   # Copy SSH key
   cat ~/.ssh/id_ed25519.pub | clip
   ```

   - Buka: https://github.com/settings/ssh/new
   - Paste key dan save

3. **Test Connection:**
   ```bash
   ssh -T git@github.com
   # Output: Hi username! You've successfully authenticated...
   ```

---

## 📝 Daily Git Workflow

### Making Changes

```bash
# 1. Check status
git status

# 2. Add changed files
git add .
# Atau specific files:
git add payment-api/src/main/java/com/payment/api/controller/PaymentController.java

# 3. Commit with message
git commit -m "feat: add payment batch processing endpoint"

# 4. Push to GitHub
git push
```

### Pull Latest Changes

```bash
# Get latest from GitHub
git pull origin main
```

### Create Feature Branch

```bash
# Create and switch to new branch
git checkout -b feature/payment-webhook

# Make changes...
git add .
git commit -m "feat: implement payment webhook handler"

# Push branch to GitHub
git push -u origin feature/payment-webhook
```

---

## 🔄 GitHub Actions (CI/CD)

Automated builds akan running setiap kali push ke GitHub!

File `.github/workflows/ci.yml` sudah dikonfigurasi untuk:

- ✅ Build dengan Maven
- ✅ Run tests
- ✅ Check code quality
- ✅ Generate coverage report

**View Build Status:**

- Buka repository di GitHub
- Click tab "Actions"
- Lihat build results

---

## 📊 Repository Settings (Recommended)

### 1. Add Repository Description

- Settings → Options
- Description: "Enterprise payment processing system"
- Topics: `java`, `spring-boot`, `payment-processing`, `microservices`

### 2. Enable Branch Protection (Optional)

- Settings → Branches
- Add rule for `main`:
  - ✅ Require pull request reviews
  - ✅ Require status checks to pass
  - ✅ Require branches to be up to date

### 3. Add Collaborators (Optional)

- Settings → Collaborators
- Invite team members

---

## 🐛 Troubleshooting

### Error: "failed to push some refs"

```bash
# Pull latest changes first
git pull origin main --rebase
git push
```

### Error: "Permission denied (publickey)"

```bash
# Check SSH key is added
ssh -T git@github.com

# If not working, use HTTPS instead
git remote set-url origin https://github.com/USERNAME/payment-processing-system.git
```

### Undo Last Commit (not pushed yet)

```bash
# Keep changes
git reset --soft HEAD~1

# Discard changes
git reset --hard HEAD~1
```

### Remove File from Git (keep locally)

```bash
git rm --cached filename
echo "filename" >> .gitignore
git commit -m "chore: remove sensitive file"
```

---

## 📚 Useful Git Commands

```bash
# View commit history
git log --oneline --graph --all

# View changes
git diff

# Undo uncommitted changes
git restore filename

# Switch branches
git checkout branch-name

# Delete branch
git branch -d branch-name

# Tag a release
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

---

## ✅ Verification Checklist

- [ ] Git installed and configured
- [ ] Repository initialized locally
- [ ] Initial commit created
- [ ] GitHub repository created
- [ ] Remote added
- [ ] Successfully pushed to GitHub
- [ ] GitHub Actions running successfully
- [ ] README.md displayed properly
- [ ] .gitignore working (no logs/target folders)

---

**Repository Anda sekarang sudah online di GitHub! 🎉**

Clone URL: `https://github.com/YOUR_USERNAME/payment-processing-system.git`
