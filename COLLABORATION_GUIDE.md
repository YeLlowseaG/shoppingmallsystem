# Collaboration Guide

**Date**: 2025-12-03

---

## 1. Git Branch Management Strategy

### Recommended: Feature Branch Workflow

```
main (or master) - Production code, only accepts merges, no direct commits
dev - Development environment code
feature/xxx - One branch per feature
```

### Workflow

```bash
# 1. Before starting new feature, pull latest code from dev
git checkout dev
git pull gitee dev

# 2. Create feature branch
git checkout -b feature/user-login

# 3. Develop on feature branch
git add .
git commit -m "feat: implement user login"

# 4. Before finishing, pull latest updates to avoid conflicts
git checkout dev
git pull gitee dev
git checkout feature/user-login
git merge dev  # Merge dev updates into your branch first

# 5. Push feature branch
git push origin feature/user-login

# 6. Merge to dev (or create Pull Request)
git checkout dev
git merge feature/user-login
git push
```

---

## 2. Best Practices to Reduce Conflicts

### Clear Division of Work

```
Suggested Division:
Developer A: Frontend (frontend/) + Product Module Backend
Developer B: Backend (backend/) + Order Module Backend
```

### Recommended Project Structure

```
shoppingmallsystem/
├── frontend/          # Frontend project (mainly Developer A)
│   ├── admin/        # Admin dashboard
│   └── h5/           # Mobile mall
├── backend/          # Backend project (mainly Developer B)
│   ├── src/
│   │   ├── modules/
│   │   │   ├── product/    # Product module (Developer A)
│   │   │   ├── order/      # Order module (Developer B)
│   │   │   ├── user/       # User module (Developer A)
│   │   │   └── payment/    # Payment module (Developer B)
├── docs/             # Documentation
├── database/         # Database scripts
└── README.md
```

---

## 3. Code Standards (Consistency is Key)

### Configuration Files to Create

1. **`.editorconfig`** - Unified editor configuration
2. **`.eslintrc.js`** - Unified code linting
3. **`.prettierrc`** - Unified code formatting
4. **`commitlint.config.js`** - Unified commit message conventions

### Git Commit Message Convention

```bash
# Format: <type>: <subject>
feat: New feature
fix: Bug fix
docs: Documentation update
style: Code style (doesn't affect code execution)
refactor: Code refactoring
test: Testing
chore: Build tools or auxiliary tools changes

# Examples
git commit -m "feat: add product list page"
git commit -m "fix: fix shopping cart quantity calculation error"
git commit -m "docs: update API documentation"
```

---

## 4. Daily Collaboration Workflow

### Morning - Before Starting Work

```bash
# 1. Pull latest code
git pull gitee dev

# 2. Check collaborator's updates
git log --oneline -5
```

### Evening - Before Committing Code

```bash
# 1. Pull latest code first
git pull gitee dev

# 2. Resolve any conflicts
# 3. Commit and push
git add .
git commit -m "feat: xxx"
git push
```

---

## 5. Communication Tools and Conventions

- **WeChat/DingTalk Group**: Daily communication
- **Documentation**: Maintain dev docs in `docs/` directory
- **TODO.md**: Record tasks and division of work
- **Daily Sync**: Recommended brief daily sync (5 minutes)

---

## 6. Common Pitfalls to Avoid

### ❌ DON'T

1. **Don't develop directly on main branch**
2. **Don't force push to shared branches** (`git push -f`)
3. **Don't commit large files** (use OSS for images, configure `.gitignore`)
4. **Don't commit sensitive information** (passwords, keys, etc.)
5. **Don't go long periods without committing** (commit at least once daily)

### ✅ DO

1. **Pull updates frequently** (before starting work each day)
2. **Commit in small increments** (commit when part of feature is done)
3. **Write clear commit messages**
4. **Communicate promptly when issues arise**
5. **Share development environment config** (DB credentials, API keys in `.env.example`)

---

## 7. Git Sync Setup

### Current Configuration

- `origin` - Pushes to both **GitHub** and **Gitee** simultaneously
- `gitee` - For manually pulling updates from Gitee

### Daily Commands

#### When you commit code
```bash
git add .
git commit -m "your commit message"
git push  # Automatically pushes to both GitHub and Gitee
```

#### When collaborator updates code on Gitee
```bash
git pull gitee main  # Pull updates from Gitee
git push             # Sync to both GitHub and Gitee
```

#### Check current status
```bash
git status
git remote -v
```

---

## 8. Conflict Resolution

### When merge conflicts occur

```bash
# 1. Pull latest code
git pull gitee dev

# 2. If conflicts appear, Git will mark them in files
# Open the conflicting files and look for:
<<<<<<< HEAD
your changes
=======
their changes
>>>>>>> branch-name

# 3. Manually resolve conflicts by choosing which code to keep

# 4. After resolving, mark as resolved
git add <resolved-file>

# 5. Complete the merge
git commit -m "merge: resolve conflicts with dev"

# 6. Push
git push
```

---

## 9. Useful Git Commands

### Check collaboration history
```bash
git log --graph --oneline --all --decorate
```

### View who changed what
```bash
git blame <file-name>
```

### Temporarily save work without committing
```bash
git stash         # Save current work
git stash pop     # Restore saved work
```

### Undo last commit (keep changes)
```bash
git reset --soft HEAD^
```

### Discard all local changes
```bash
git reset --hard HEAD
```

---

## 10. Tips for Success

1. **Communicate early and often** - Don't wait until there's a problem
2. **Keep commits atomic** - One commit = one logical change
3. **Test before pushing** - Make sure your code works
4. **Review each other's code** - Learn and improve together
5. **Document as you go** - Update docs when adding features
6. **Use descriptive branch names** - `feature/user-auth` not `fix-stuff`
7. **Pull before you push** - Always sync before pushing your changes

---

## Questions or Issues?

If you encounter any Git-related issues during collaboration, refer to this guide or communicate with your team member immediately.

Happy coding! 🚀
