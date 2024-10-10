import minecraft_launcher_lib

current_max = 0


def set_status(status: str):
    print(status)


def set_progress(progress: int):
    if current_max != 0:
        print(f"{progress}/{current_max}")


def set_max(new_max: int):
    global current_max
    current_max = new_max


callback = {
    "setStatus": set_status,
    "setProgress": set_progress,
    "setMax": set_max
}
minecraft_directory = "minecraft"

minecraft_launcher_lib.fabric.install_fabric("1.20.1", minecraft_directory, callback=callback)
