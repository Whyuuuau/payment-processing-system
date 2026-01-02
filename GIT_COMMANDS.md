# Git Quick Reference - Payment Processing System

## 📋 Quick Commands Cheat Sheet

### Initial Setup (One Time)

```bash
# Run automated setup
git-setup.bat

# Or manual setup:
git init
git config user.name "Your Name"
git config user.email "your.email@example.com"
git add .
git commit -m "Initial commit: Payment Processing System"
```

### Daily Workflow

#### Check Status

```bash
git status                    # See modified files
git diff                      # See actual changes
git log --oneline -10         # See last 10 commits
```

#### Make Changes

```bash
# Stage specific files
git add payment-api/src/main/java/com/payment/api/controller/PaymentController.java

# Stage all changes
git add .

# Stage specific folders
git add payment-core/

# Unstage files
git restore --staged filename
```

#### Commit Changes

```bash
# Commit with message
git commit -m "feat: add webhook support for payment notifications"

# Commit all modified files (skip staging)
git commit -am "fix: resolve concurrent modification issue"

# Amend last commit (if not pushed)
git commit --amend -m "Updated message"
```

#### Push to GitHub

```bash
# Push to main branch
git push

# Force push (DANGER - use only if you know what you're doing)
git push --force

# Push new branch
git push -u origin feature/new-feature
```

#### Pull from GitHub

```bash
# Pull latest changes
git pull

# Pull with rebase (cleaner history)
git pull --rebase
```

---

## 🌿 Branching

### Create Branch

```bash
# Create and switch to new branch
git checkout -b feature/payment-batch-processing

# Or using newer syntax
git switch -c feature/payment-batch-processing
```

### Switch Branch

```bash
git checkout main
git switch develop
```

### List Branches

```bash
git branch                    # Local branches
git branch -r                 # Remote branches
git branch -a                 # All branches
```

### Delete Branch

```bash
# Delete local branch
git branch -d feature/old-feature

# Force delete
git branch -D feature/old-feature

# Delete remote branch
git push origin --delete feature/old-feature
```

### Merge Branch

```bash
# Merge feature into current branch
git checkout main
git merge feature/payment-batch-processing

# Merge with no fast-forward (keep branch history)
git merge --no-ff feature/payment-batch-processing
```

---

## 🔄 Undo Changes

### Discard Local Changes

```bash
# Discard changes in specific file
git restore filename

# Discard all changes
git restore .

# Discard changes in folder
git restore payment-api/
```

### Undo Commits

```bash
# Undo last commit, keep changes
git reset --soft HEAD~1

# Undo last commit, discard changes
git reset --hard HEAD~1

# Undo last 3 commits, keep changes
git reset --soft HEAD~3

# Revert a commit (creates new commit)
git revert abc123
```

### Clean Untracked Files

```bash
# Show what would be deleted
git clean -n

# Delete untracked files
git clean -f

# Delete untracked files + directories
git clean -fd
```

---

## 🏷️ Tags & Releases

### Create Tag

```bash
# Lightweight tag
git tag v1.0.0

# Annotated tag (recommended)
git tag -a v1.0.0 -m "Release version 1.0.0 - Initial production release"

# Tag specific commit
git tag -a v1.0.0 abc123 -m "Release v1.0.0"
```

### Push Tags

```bash
# Push specific tag
git push origin v1.0.0

# Push all tags
git push --tags
```

### List & Delete Tags

```bash
# List tags
git tag

# Delete local tag
git tag -d v1.0.0

# Delete remote tag
git push origin --delete v1.0.0
```

---

## 🔍 Viewing History

### Log Commands

```bash
# Compact log
git log --oneline

# Graphical log
git log --oneline --graph --all

# Last N commits
git log -10

# Commits by author
git log --author="Your Name"

# Commits in date range
git log --since="2026-01-01" --until="2026-01-02"

# Changes in specific file
git log -p payment-api/src/main/java/com/payment/api/controller/PaymentController.java
```

### Show Commit Details

```bash
# Show last commit
git show

# Show specific commit
git show abc123

# Show files changed in commit
git show --name-only abc123
```

---

## 🚨 Emergency Commands

### Accidentally Committed Sensitive Data

```bash
# Remove from last commit (if not pushed)
git rm --cached secrets.yml
git commit --amend --no-edit

# If already pushed (DANGER - rewrites history)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch secrets.yml" \
  --prune-empty --tag-name-filter cat -- --all
git push --force
```

### Lost Commits (Reflog)

```bash
# Show reflog
git reflog

# Recover deleted commit
git cherry-pick abc123

# Reset to previous state
git reset --hard HEAD@{5}
```

### Resolve Merge Conflicts

```bash
# See conflicted files
git status

# After fixing conflicts:
git add .
git commit -m "fix: resolve merge conflicts"

# Abort merge
git merge --abort
```

---

## 🔧 Configuration

### Check Configuration

```bash
# List all config
git config --list

# Specific config
git config user.name
git config user.email
```

### Set Configuration

```bash
# Local (current repository only)
git config user.name "Your Name"

# Global (all repositories)
git config --global user.name "Your Name"

# Set default editor
git config --global core.editor "code --wait"

# Set default branch name
git config --global init.defaultBranch main
```

### Aliases (Shortcuts)

```bash
# Create aliases
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.lg "log --oneline --graph --all"

# Use aliases
git st              # Same as git status
git lg              # Nice graph log
```

---

## 📤 GitHub Specific

### Clone Repository

```bash
git clone https://github.com/username/payment-processing-system.git
cd payment-processing-system
```

### Add/Change Remote

```bash
# Add remote
git remote add origin https://github.com/username/payment-processing-system.git

# Change remote URL
git remote set-url origin https://github.com/username/payment-processing-system.git

# View remotes
git remote -v
```

### Sync Fork

```bash
# Add upstream
git remote add upstream https://github.com/original/payment-processing-system.git

# Fetch upstream
git fetch upstream

# Merge upstream changes
git merge upstream/main
```

---

## 💡 Best Practices

### Commit Messages Format

```
<type>: <subject>

<body>

<footer>
```

**Types:**

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `style:` Formatting
- `refactor:` Code refactoring
- `test:` Adding tests
- `chore:` Build/config changes

**Examples:**

```bash
git commit -m "feat: add payment batch processing endpoint"

git commit -m "fix: resolve OptimisticLockingFailureException in concurrent updates

- Implement retry logic with exponential backoff
- Add comprehensive tests for concurrent scenarios
- Update documentation"

git commit -m "docs: update README with deployment instructions"
```

### Branch Naming

```
feature/payment-webhook
fix/concurrent-update-issue
chore/update-dependencies
docs/api-documentation
```

---

## ⚡ Performance Tips

### Large Files

```bash
# See large files in repo
git rev-list --objects --all | \
  git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | \
  sort -k 3 -n -r | head -20
```

### Reduce Clone Size

```bash
# Shallow clone (faster)
git clone --depth 1 https://github.com/username/payment-processing-system.git
```

### Speed up git status

```bash
# Enable file system monitor
git config core.fsmonitor true
```

---

**💾 Save this file for quick reference!**
