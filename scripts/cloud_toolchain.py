import concurrent.futures, hashlib, os, pathlib, shutil, tarfile, urllib.request, zipfile

root = pathlib.Path('/workspace/.tools')
cache = root / 'downloads'
cache.mkdir(parents=True, exist_ok=True)

def fetch(url, name):
    target = cache / name
    if not target.exists():
        print('Downloading', name, flush=True)
        with urllib.request.urlopen(url, timeout=120) as src, target.open('wb') as dst:
            shutil.copyfileobj(src, dst)
    return target

def checked(url, name, checksum_url=None, expected=None, algorithm='sha256'):
    p = fetch(url, name)
    if checksum_url:
        expected = urllib.request.urlopen(checksum_url, timeout=60).read().decode().split()[0]
    actual = hashlib.file_digest(p.open('rb'), algorithm).hexdigest()
    if actual != expected:
        p.unlink()
        raise RuntimeError('Checksum mismatch: ' + name)
    print('Verified', name, flush=True)
    return p

def unpack_zip(p, dest):
    with zipfile.ZipFile(p) as z:
        z.extractall(dest)
        for info in z.infolist():
            mode = info.external_attr >> 16
            if mode:
                (dest / info.filename).chmod(mode)

def jdk():
    base = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.16%2B8/'
    name = 'OpenJDK17U-jdk_x64_linux_hotspot_17.0.16_8.tar.gz'
    p = checked(base+name, name, base+name+'.sha256.txt')
    if not (root/'jdk-17.0.16+8/bin/javac').exists():
        with tarfile.open(p) as t:
            t.extractall(root, filter='data')

def gradle():
    url = 'https://services.gradle.org/distributions/gradle-8.11.1-bin.zip'
    p = checked(url, 'gradle-8.11.1-bin.zip', url+'.sha256')
    if not (root/'gradle-8.11.1/bin/gradle').exists():
        unpack_zip(p, root)

def sdk():
    name = 'commandlinetools-linux-16111833_latest.zip'
    p = checked('https://dl.google.com/android/repository/'+name, name,
                expected='e025545c62a8e64c7559119566a569fb1dec5f60', algorithm='sha1')
    dest=root/'android-sdk'/'cmdline-tools'
    dest.mkdir(parents=True, exist_ok=True)
    if not (dest/'latest').exists():
        unpack_zip(p, dest)
        (dest/'cmdline-tools').rename(dest/'latest')

with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:
    for f in [pool.submit(jdk),pool.submit(gradle),pool.submit(sdk)]:
        f.result()
print('Bootstrap complete', flush=True)
