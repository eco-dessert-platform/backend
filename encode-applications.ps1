# application-[].yml 파일을 base64로 변경해주는 프로그램

# 사용법
##
## backend 루트에서 "./파일명" 입력 // ex) ./encode-applications.ps1
## src/main/resources/ 경로에 [].base.txt 파일 생성 됨

# 현재 스크립트 위치 기준으로 상대경로 설정
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$resourcePath = Join-Path $scriptDir "src\main\resources"

# Get all application*.yml files
Get-ChildItem -Path $resourcePath -Filter "application*.yml" | ForEach-Object {
    $file = $_
    $fileName = $file.Name
    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($fileName)  # application, application-dev 등

    # 출력 파일명 설정
    $outputFile = Join-Path $resourcePath "$baseName.base.txt"

    # Base64 인코딩
    $base64 = [Convert]::ToBase64String([System.IO.File]::ReadAllBytes($file.FullName))

    # 저장
    Set-Content -Path $outputFile -Value $base64

    Write-Host "✅ $fileName -> $($baseName).base.txt"
}
