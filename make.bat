echo "cleanup\n"

del *.jar
del bin\bouncycastle\*.*
del bin\xts\*.*
del bin\bouncycastle
del bin\xts
del bin

echo "creating bin directory\n"

mkdir "bin\"

echo "Building XTS-AES.jar\n"

cd src
javac xts/*.java bouncycastle/*.java -d ../bin/
cd ..\bin\
jar cvfm ../XTS-AES.jar ../Manifest.txt ./

echo "Running the jar file\n"
cd ..
java -jar XTS-AES.jar
