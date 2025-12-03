# Branch Strategy for Shopping Mall System

**Project Type**: Commercial Project
**Team Size**: 2 Developers
**Strategy**: Main + Dev + Feature Branches

---

## Branch Structure

```
main (Production - Stable, deployable code)
  └── dev (Development - Daily development and testing)
       ├── feature/product-module (Developer A)
       ├── feature/order-module (Developer B)
       ├── feature/user-auth (Developer A)
       └── feature/payment-integration (Developer B)
```

---

## Branch Descriptions

### 1. main Branch
- **Purpose**: Production-ready code, deployed to client
- **Protection**: Never commit directly to main
- **Updates**: Only accepts merges from dev after thorough testing
- **Deployment**: Every merge to main = a release version

### 2. dev Branch
- **Purpose**: Integration branch for ongoing development
- **Testing**: All features are tested here before going to main
- **Updates**: Accepts merges from feature branches
- **Daily Work**: This is where most collaboration happens

### 3. feature/* Branches
- **Purpose**: Individual feature development
- **Naming**: `feature/<feature-name>` (e.g., `feature/product-list`)
- **Lifetime**: Created from dev, deleted after merge
- **Scope**: One feature per branch

---

## Daily Workflow

### Starting a New Feature

```bash
# 1. Switch to dev and pull latest code
git checkout dev
git pull gitee dev

# 2. Create feature branch from dev
git checkout -b feature/product-list

# 3. Develop your feature
# ... coding ...

# 4. Commit regularly
git add .
git commit -m "feat: add product list page"
git push origin feature/product-list
```

### Completing a Feature

```bash
# 1. Make sure dev is up to date in your feature branch
git checkout dev
git pull gitee dev
git checkout feature/product-list
git merge dev  # Merge latest dev into your feature

# 2. Resolve any conflicts if they exist

# 3. Merge feature into dev
git checkout dev
git merge feature/product-list

# 4. Push to remote
git push

# 5. Delete feature branch (optional, keep it clean)
git branch -d feature/product-list
git push origin --delete feature/product-list
```

### Releasing to Production

```bash
# Only after thorough testing in dev branch

# 1. Switch to main
git checkout main
git pull gitee main

# 2. Merge dev into main
git merge dev

# 3. Tag the release (recommended)
git tag -a v1.0.0 -m "Release version 1.0.0"

# 4. Push to remote
git push
git push --tags
```

---

## Branch Naming Conventions

### Feature Branches
```
feature/user-login
feature/product-crud
feature/order-management
feature/payment-integration
```

### Bug Fix Branches (if needed)
```
fix/cart-calculation
fix/login-redirect
```

### Hotfix Branches (urgent production fixes)
```
hotfix/critical-payment-bug
```

---

## Common Scenarios

### Scenario 1: Both developers working on different features

**Developer A**:
```bash
git checkout dev
git pull gitee dev
git checkout -b feature/product-module
# ... develop ...
git push origin feature/product-module
```

**Developer B**:
```bash
git checkout dev
git pull gitee dev
git checkout -b feature/order-module
# ... develop ...
git push origin feature/order-module
```

**No conflicts!** Each works on their own branch.

---

### Scenario 2: Developer A finishes first

**Developer A merges to dev**:
```bash
git checkout dev
git pull gitee dev
git merge feature/product-module
git push
```

**Developer B syncs the changes**:
```bash
git checkout dev
git pull gitee dev
git checkout feature/order-module
git merge dev  # Get A's changes into B's feature branch
```

---

### Scenario 3: Ready to release to client

**After both features are merged and tested in dev**:
```bash
git checkout main
git pull gitee main
git merge dev
git tag -a v1.0.0 -m "First release"
git push
git push --tags
```

---

## Important Rules

### ❌ NEVER Do This
1. Never commit directly to `main` branch
2. Never force push to `main` or `dev` (`git push -f`)
3. Never delete `main` or `dev` branches
4. Never merge untested code to `main`

### ✅ ALWAYS Do This
1. Create feature branches from `dev`
2. Pull latest `dev` before creating feature branch
3. Merge `dev` into your feature branch regularly (stay up to date)
4. Test thoroughly in `dev` before merging to `main`
5. Communicate with your teammate before big merges

---

## Quick Reference Commands

### Check current branch
```bash
git branch
git status
```

### Switch branches
```bash
git checkout dev
git checkout main
git checkout -b feature/new-feature  # Create and switch
```

### Update current branch with latest remote
```bash
git pull gitee <branch-name>
```

### View all branches
```bash
git branch -a  # Local and remote
```

### Delete a branch
```bash
git branch -d feature/old-feature     # Local
git push origin --delete feature/old-feature  # Remote
```

---

## Visualization

```
Time →

main:    v1.0.0 ────────────────── v1.1.0 ─────────────────→
                                     ↑
                                     │ (merge after testing)
                                     │
dev:     ─────●────●────●────●────●─┴─────●────●───────────→
              ↑    ↑    ↑    ↑    ↑       ↑    ↑
              │    │    │    │    │       │    │
feature/A:   ─┴─●─●┴─→ │    │    │       │    │
                        │    │    │       │    │
feature/B:   ──────────┴─●─●┴─→  │       │    │
                                  │       │    │
feature/C:   ────────────────────┴─●─●─●─┴──→ │
                                               │
feature/D:   ──────────────────────────────────┴─●─●─→

● = commit
→ = ongoing work
```

---

## Team Communication

Before merging to `dev` or `main`:
1. Notify your teammate in the group chat
2. Ensure your tests pass
3. Check if anyone else is about to merge

This prevents merge conflicts and keeps everyone in sync!

---

**Questions?** Refer to `COLLABORATION_GUIDE.md` or discuss with your teammate.
