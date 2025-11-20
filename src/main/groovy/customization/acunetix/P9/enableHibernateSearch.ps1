$path = "$env:RootPath\P3\profiles\P9 Server\path\P9 Server.xml"
$xmlDoc = [System.Xml.XmlDocument](Get-Content -Path $path)
$propNode = $xmlDoc.SelectSingleNode("//preferences/root/node/path[@name='config']/node[@name='properties']/map")
$key = "hibernate.search.enabled"
$node = $propNode.SelectSingleNode("/entry[@key='$key']")
if (!$node) { $node = $propNode.AppendChild($xmlDoc.CreateElement("entry")); $node.SetAttribute("key", "$key") }
$node.SetAttribute("value", "true")
$xmlDoc.Save($path)