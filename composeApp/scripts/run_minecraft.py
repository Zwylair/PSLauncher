import os
import sys
import uuid
import hashlib
import subprocess
import minecraft_launcher_lib


def uuid_from_string(val: str):
    hex_string = hashlib.md5(val.encode("UTF-8")).hexdigest()
    return uuid.UUID(hex=hex_string)


cwd = os.getcwd()
args = sys.argv[1:]
minecraft_directory = "minecraft/"
options = {
    "username": args[0],
    "uuid": str(uuid_from_string(args[0])),
    "token": ""
}
selected_version = ""

os.chdir(minecraft_directory)

for version in minecraft_launcher_lib.utils.get_installed_versions(os.getcwd()):
    if "fabric" in version["id"] and "1.20.1" in version["id"]:
        selected_version = version["id"]

minecraft_command = minecraft_launcher_lib.command.get_minecraft_command(selected_version, os.getcwd(), options)

subprocess.run(minecraft_command)
os.chdir(cwd)
