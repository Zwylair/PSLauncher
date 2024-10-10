import os
import subprocess
import minecraft_launcher_lib

minecraft_directory = "minecraft/"
selected_version = ""

for version in minecraft_launcher_lib.utils.get_installed_versions(minecraft_directory):
    if "fabric" in version["id"] and "1.20.1" in version["id"]:
        selected_version = version["id"]

options = minecraft_launcher_lib.utils.generate_test_options()
minecraft_command = minecraft_launcher_lib.command.get_minecraft_command(selected_version, minecraft_directory, options)

subprocess.run(minecraft_command)
