import os

old = 'chest_'
new = 'chest_'

def renameFiles(root, files):
	for fname in files:
		if old in fname:
			print(os.path.join(root, fname))
			os.rename(os.path.join(root, fname), os.path.join(root, fname.replace(old, new)))

def renameDir(dirPath):
	for root, dirs, files in os.walk(dirPath):
		renameFiles(root, files)
		for dname in dirs:
			renameDir(os.path.join(root, dname))
		renameFiles(root, dirs)

renameDir('.')