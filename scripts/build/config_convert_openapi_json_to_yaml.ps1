# PowerShell Script to Convert JSON Files to YAML
# Processes ONLY files in the root directory that start with "openapi"

# Set the correct encoding for special characters
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Set standard colors for batch-like experience
$InfoColor = "White"      # Standard information
$SuccessColor = "Green"   # Success messages
$WarningColor = "Yellow"  # Warnings and cautions
$ErrorColor = "Red"       # Errors
$HighlightColor = "Cyan"  # Highlights and important information

Write-Host ""
Write-Host "----------------------------------------------------------------------" -ForegroundColor $HighlightColor
Write-Host "|  CONVERTING OPENAPI JSON TO YAML                                   |" -ForegroundColor $HighlightColor
Write-Host "----------------------------------------------------------------------" -ForegroundColor $HighlightColor
Write-Host ""

# Check if the powershell-yaml module is available
$hasYamlModule = $null -ne (Get-Module -ListAvailable -Name powershell-yaml)

# If neither is available
if (-not $hasYamlModule)
{
    Write-Host "WARNING: No conversion tools available!" -ForegroundColor $ErrorColor

    $installChoice = Read-Host "Do you want to install powershell-yaml [y]? (y/n) [default: y]"
    if ([string]::IsNullOrEmpty($installChoice)) {
        $installChoice = "y"
    }

    if ($installChoice -eq "y")
    {
        Write-Host "Installing powershell-yaml module..." -ForegroundColor $HighlightColor
        try
        {
            Install-Module -Name powershell-yaml -Scope CurrentUser -Force -AllowClobber -ErrorAction Stop
            Write-Host "powershell-yaml module successfully installed!" -ForegroundColor $SuccessColor
            $hasYamlModule = $true
        }
        catch
        {
            Write-Host "Error during module installation: $_" -ForegroundColor $ErrorColor
            Write-Host "Alternative installation methods:" -ForegroundColor $WarningColor
            Write-Host "1. Try running PowerShell with administrator rights and run:" -ForegroundColor $WarningColor
            Write-Host "   Install-Module -Name powershell-yaml -Scope AllUsers -Force -AllowClobber" -ForegroundColor $InfoColor
            Write-Host "2. Or try with NuGet provider if available:" -ForegroundColor $WarningColor
            Write-Host "   Install-PackageProvider -Name NuGet -Force" -ForegroundColor $InfoColor
            Write-Host "   Install-Module -Name powershell-yaml -Force -AllowClobber -Scope CurrentUser" -ForegroundColor $InfoColor
            Write-Host "3. If TLS issues occur, try first:" -ForegroundColor $WarningColor
            Write-Host "   [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12" -ForegroundColor $InfoColor

            exit 1
        }
    }
    else
    {
        Write-Host "Invalid choice. Cannot proceed without a conversion tool. Exiting..." -ForegroundColor $ErrorColor
        exit 1
    }
}


# Find JSON files ONLY in the current directory that start with "openapi"
$jsonFiles = Get-ChildItem -Path . -File | Where-Object { $_.Name -match "^openapi.*\.json$" }

# Create openapi directory if it doesn't exist
$openapiDir = Join-Path (Get-Location) "openapi"
if (-not (Test-Path -Path $openapiDir -PathType Container)) {
    Write-Host "Creating 'openapi' directory..." -ForegroundColor $InfoColor
    New-Item -Path $openapiDir -ItemType Directory | Out-Null
    Write-Host "'openapi' directory created successfully." -ForegroundColor $SuccessColor
} else {
    Write-Host "'openapi' directory already exists." -ForegroundColor $InfoColor
}

if ($jsonFiles.Count -eq 0)
{
    Write-Host "No JSON files found in the root directory that start with 'openapi'." -ForegroundColor $WarningColor
}
else
{
    Write-Host "Found $( $jsonFiles.Count ) JSON files to convert in the root directory." -ForegroundColor $SuccessColor
    Write-Host ""

    foreach ($file in $jsonFiles)
    {
        $inputFile = $file.FullName
        $baseFileName = $file.Name -replace '\.json$', '.yaml'
        $outputFile = Join-Path $openapiDir $baseFileName

        Write-Host "Converting:..." -ForegroundColor $InfoColor
        Write-Host "from: $inputFile" -ForegroundColor $InfoColor
        Write-Host "to: $outputFile" -ForegroundColor $InfoColor

        try
        {
            # If yq is available, use it
            if ($hasYq)
            {
                yq -P eval $inputFile > $outputFile
            }
            # Otherwise, use powershell-yaml module
            elseif ($hasYamlModule)
            {
                # Import the module if it's not already imported
                if (-not (Get-Module -Name powershell-yaml))
                {
                    Import-Module powershell-yaml
                }

                $jsonContent = Get-Content -Path $inputFile -Raw | ConvertFrom-Json
                $yamlContent = ConvertTo-Yaml -Data $jsonContent -OutFile $outputFile -Force
            }
            Write-Host "Completed" -ForegroundColor $SuccessColor

            try {
                Remove-Item -Path $inputFile -Force
                Write-Host ""
                Write-Host "Deleted original JSON file: $inputFile" -ForegroundColor $WarningColor
            }
            catch {
                Write-Host ""
                Write-Host "Error deleting JSON file: $_" -ForegroundColor $ErrorColor
            }

            Write-Host ""
        }
        catch
        {
            Write-Host "Error during conversion: $_" -ForegroundColor $ErrorColor
            Write-Host ""
            exit 1
        }
    }
}
