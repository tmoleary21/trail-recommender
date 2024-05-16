from time import time
import subprocess

def main():
    start_time = time()
    subprocess.run('/s/bach/n/under/tmoleary/cs555/term-project/sample_run.sh', shell=True)
    end_time = time()

    print(f'\nStart Time: {start_time} , End Time: {end_time}')     

if __name__ == "__main__":
    main()
