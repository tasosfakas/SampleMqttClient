param ($List, $Values , $Url)

#connect-PnPOnline -url https://arcelormittal.sharepoint.com/sites/brm-powerplatform/Development/SmartBoardMQTT -UseWebLogin 
connect-PnPOnline -url $Url -UseWebLogin 

Get-PnPList

# $Values = $Values.Substring(1);
# $Values = $Values.Substring(0,$Values.Length-1);

$vars = @{}
$Values.Trim() -split "#" | % {
  $key, $value = $_ -split '\s*=\s*'
  $vars[$key] = $value
}

Write-Output $Values
Write-Output "################################"
Write-Output $vars

#Add-PnPListItem -List $List  -ContentType Item -values $vars

Add-PnPListItem -List $List -ContentType Item -values  $vars


