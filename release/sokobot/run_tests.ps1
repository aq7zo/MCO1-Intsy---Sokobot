# run_tests.ps1
$maps = @(
    "twoboxes1", "twoboxes2", "twoboxes3",
    "threeboxes1", "threeboxes2", "threeboxes3",
    "fourboxes1", "fourboxes2", "fourboxes3",
    "fiveboxes1", "fiveboxes2", "fiveboxes3",
    "original1", "original2", "original3",
    "testlevel"
)

Write-Host "Compiling..."
javac -cp src src/main/Driver.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!"
    exit 1
}

Write-Host "Running tests..."
$format = "{0,-15} | {1,-10} | {2,-10} | {3,-10}"
Write-Host ($format -f "Map Name", "Status", "Time (s)", "Moves")
Write-Host ("-" * 55)

$passed = 0
$total = 0

foreach ($map in $maps) {
    $total++
    if (Test-Path temp.txt) { Remove-Item temp.txt }
    
    # Run in check mode (which exits with code 1)
    $null = java -cp src main.Driver $map check
    
    if (Test-Path temp.txt) {
        $content = Get-Content temp.txt -Raw
        $parts = $content.Split(" ")
        if ($parts.Length -ge 3) {
            $time = $parts[0]
            $status = $parts[1]
            $moves = $parts[2]
            Write-Host ($format -f $map, $status, $time, $moves)
            if ($status -eq "PASS") {
                $passed++
            }
        } else {
            Write-Host ($format -f $map, "ERROR", "-", "-")
        }
    } else {
        Write-Host ($format -f $map, "TIMEOUT/FAIL", "-", "-")
    }
}

Write-Host ("-" * 55)
Write-Host "Passed: $passed / $total"
