# Branch Protection Rules 설정 가이드

이 문서는 테스트가 통과해야만 PR을 merge할 수 있도록 GitHub Branch Protection Rules를 설정하는 방법을 안내합니다.

## 1. Branch Protection Rules 설정

### 1.1 GitHub Repository 설정 페이지로 이동

1. GitHub 저장소 페이지로 이동
2. **Settings** 탭 클릭
3. 왼쪽 사이드바에서 **Branches** 클릭

### 1.2 Branch Protection Rule 추가

1. **Add branch protection rule** 버튼 클릭
2. 다음과 같이 설정:

#### Branch name pattern
```
main
```
또는 여러 브랜치를 보호하려면:
```
main
develop
```

#### Protect matching branches 설정

다음 옵션들을 **체크**하세요:

##### ✅ Require a pull request before merging
- **Required approvals**: 1 (또는 팀 규모에 맞게 조정)
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Require review from Code Owners (선택사항)

##### ✅ Require status checks to pass before merging
- ✅ Require branches to be up to date before merging
- **Status checks that are required**:
  - `Test Gate (Required for Merge)` - 반드시 추가!
  - `Test Summary` - 반드시 추가!
  - `Build and Test` (CI workflow)
  - `Test Results (Required)`

> **중요**: Status checks는 해당 workflow가 최소 한 번 실행된 후에 목록에 나타납니다.

##### ✅ Require conversation resolution before merging
- PR의 모든 코멘트가 해결되어야 merge 가능

##### ✅ Require signed commits (선택사항)
- 커밋 서명이 필요한 경우 체크

##### ✅ Require linear history (권장)
- Merge commit 대신 Squash merge 또는 Rebase merge 사용

##### ✅ Include administrators
- 관리자도 동일한 규칙 적용 (권장)

##### ✅ Restrict who can push to matching branches (선택사항)
- 특정 팀이나 사용자만 직접 push 가능하도록 제한

##### ✅ Allow force pushes (주의!)
- 특별한 경우가 아니면 체크 해제 권장

##### ✅ Allow deletions (주의!)
- main 브랜치 삭제 방지를 위해 체크 해제 권장

3. **Create** 또는 **Save changes** 버튼 클릭

---

## 2. Status Checks 활성화

### 2.1 첫 PR 생성

Branch protection rules를 설정한 후, 최소 한 번은 PR을 생성하고 CI workflow가 실행되어야 status checks가 목록에 나타납니다.

1. 새로운 브랜치 생성:
```bash
git checkout -b test/branch-protection
git commit --allow-empty -m "test: branch protection setup"
git push -u origin test/branch-protection
```

2. GitHub에서 PR 생성
3. CI workflows가 실행될 때까지 대기
4. Settings → Branches로 돌아가서 status checks 추가

### 2.2 필수 Status Checks

다음 checks들이 **반드시** required로 설정되어야 합니다:

- ✅ **Test Gate (Required for Merge)** - 모든 테스트 실행
- ✅ **Test Summary** - 테스트 결과 요약
- ✅ **Build and Test** - 전체 빌드 및 테스트
- ✅ **Test Results (Required)** - 테스트 결과 게시

---

## 3. 워크플로우 동작 확인

### 3.1 테스트 실패 시나리오

1. 의도적으로 테스트를 실패하도록 코드 수정
2. PR 생성
3. CI가 실행되고 테스트 실패
4. **Merge 버튼이 비활성화**되어야 함
5. 에러 메시지: "Required status check 'Test Gate (Required for Merge)' is failing"

### 3.2 테스트 성공 시나리오

1. 모든 테스트가 통과하는 코드 작성
2. PR 생성
3. CI가 실행되고 모든 테스트 통과
4. Coverage 요구사항 충족 (최소 60%)
5. **Merge 버튼이 활성화**되어야 함

---

## 4. 팀 워크플로우 가이드

### 4.1 개발자 체크리스트

PR을 생성하기 전:

```bash
# 로컬에서 테스트 실행
./gradlew test

# Coverage 확인
./gradlew jacocoTestReport
# 리포트: build/reports/jacoco/test/html/index.html

# Coverage 검증
./gradlew jacocoTestCoverageVerification
```

### 4.2 PR 생성 후

1. ✅ 모든 CI checks가 통과할 때까지 대기
2. ✅ Coverage 리포트 확인
3. ✅ Code review 요청
4. ✅ 모든 코멘트 해결
5. ✅ Merge

### 4.3 테스트 실패 시 대응

1. GitHub Actions 탭에서 실패한 테스트 로그 확인
2. 로컬에서 테스트 재현 및 수정
3. 수정 후 commit & push
4. CI가 자동으로 다시 실행됨

---

## 5. Coverage 요구사항

현재 설정된 최소 coverage:

- **Overall Coverage**: 60%
- **Changed Files Coverage**: 70%
- **Class Coverage**: 50%

### Coverage 향상 팁

1. 모든 새로운 기능에 테스트 작성
2. Edge cases 테스트
3. Error handling 테스트
4. Integration tests 추가

---

## 6. 예외 상황 처리

### 6.1 긴급 Hot Fix

긴급한 경우, 다음 방법 사용:

1. **방법 1**: Administrator 권한으로 branch protection 임시 비활성화
   - Settings → Branches → Edit rule
   - "Include administrators" 체크 해제
   - Hot fix merge 후 다시 활성화

2. **방법 2**: Fast-forward merge 허용
   - 로컬에서 모든 테스트 통과 확인
   - Direct push (branch protection에서 특정 팀 허용 시)

### 6.2 CI 장애

GitHub Actions가 다운된 경우:

1. Status check 일시적으로 비활성화
2. 로컬 테스트 결과로 판단
3. CI 복구 후 규칙 재활성화

---

## 7. 모니터링 및 유지보수

### 7.1 정기 점검 항목

- [ ] Branch protection rules 설정 확인 (월 1회)
- [ ] Required status checks 목록 업데이트
- [ ] Coverage 요구사항 재검토 (분기 1회)
- [ ] 팀원들의 피드백 수집

### 7.2 메트릭 추적

다음 메트릭을 추적하여 프로세스 개선:

- PR당 평균 테스트 실패 횟수
- 전체 test suite 실행 시간
- Coverage 트렌드
- Hot fix 빈도

---

## 8. 문제 해결

### Q: Status check가 목록에 나타나지 않아요

**A**: Workflow가 최소 한 번 실행되어야 합니다. 테스트 PR을 생성하고 CI가 실행될 때까지 기다리세요.

### Q: Merge 버튼이 활성화되지 않아요

**A**: 다음을 확인하세요:
1. 모든 required status checks가 통과했는지
2. PR이 up-to-date 상태인지
3. Conflicts가 없는지
4. Required approvals를 받았는지

### Q: Coverage가 요구사항을 충족하지 못해요

**A**:
1. 새로운 코드에 테스트 추가
2. `./gradlew jacocoTestReport`로 어떤 부분이 커버되지 않았는지 확인
3. 필요시 coverage 요구사항을 조정 (`.github/workflows/test-required.yml`)

### Q: 테스트가 로컬에서는 통과하는데 CI에서 실패해요

**A**:
1. 환경 차이 확인 (데이터베이스, 의존성 버전 등)
2. CI 로그 자세히 확인
3. Docker로 CI 환경 재현
4. Flaky tests 확인 및 수정

---

## 9. 참고 자료

- [GitHub Branch Protection Rules 공식 문서](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [GitHub Actions Status Checks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks)
- [JaCoCo Coverage](https://www.jacoco.org/jacoco/trunk/doc/)

---

**작성일**: 2025-11-13
**버전**: 1.0.0
**업데이트**: 변경사항 발생 시 이 문서를 업데이트하세요
