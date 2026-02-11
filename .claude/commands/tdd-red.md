Write a failing test (TDD Red phase).

## Process

1. **Write the test first** - describe expected behavior
2. **Run the test** - it MUST fail
3. **Verify failure is for the right reason**

```bash
# Run specific test
./gradlew test --tests "*TestName*"

# Expected: FAILED
```

## Test Template

```kotlin
@Test
fun `feature_condition_expectedResult`() {
    // Given
    val input = ...
    
    // When
    val result = systemUnderTest.doSomething(input)
    
    // Then
    assertEquals(expected, result)
}
```

## Next Step

Once test fails correctly:
```
/tdd-green TestName
```
