import os, pathlib, subprocess, urllib.parse

root=pathlib.Path('/workspace/.tools')
sdk=root/'android-sdk'
env=os.environ.copy()
env['JAVA_HOME']=str(root/'jdk-17.0.16+8')
env['ANDROID_HOME']=str(sdk)
env['ANDROID_USER_HOME']=str(root/'android-user')
sdk.mkdir(parents=True,exist_ok=True)
(sdk/'licenses').mkdir(exist_ok=True)
# The standard Android SDK license is accepted for this development setup.
license_file = sdk/'licenses'/'android-sdk-license'
existing = license_file.read_text().splitlines() if license_file.exists() else []
required = ['8933bad161af4178b1185d1a37fbf41ea5269c55', 'd56f5187479451eabf01fb78af6dfcb131a6481e', '24333f8a63b6825ea9c5514f83c2829b004d1fee']
license_file.write_text('\n'.join(dict.fromkeys(existing + required)) + '\n')
args=[str(sdk/'cmdline-tools/latest/bin/sdkmanager'),'--sdk_root='+str(sdk)]
proxy=urllib.parse.urlsplit(env.get('HTTPS_PROXY',''))
if proxy.hostname:
 args+=['--proxy=http','--proxy_host='+proxy.hostname,'--proxy_port='+str(proxy.port or 80)]
env['JAVA_OPTS']='-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts'
args+=['platforms;android-35','build-tools;35.0.0','platform-tools','emulator','system-images;android-29;default;x86_64']
raise SystemExit(subprocess.call(args,env=env,stdin=subprocess.DEVNULL))
