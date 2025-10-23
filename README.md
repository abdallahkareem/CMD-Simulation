# 🖥️ Project – Command Line Interpreter

## 📘 Overview
This project implements a **Command Line Interpreter (CLI)** in Java that simulates basic shell operations similar to Linux and Windows terminals.  
It supports file and directory manipulation, text redirection, file compression, and command output handling.

---

## 🧩 Features

### 🔹 Core Capabilities
- Parse and execute built-in commands.
- Support for **arguments with spaces** using quotes `" "`.
- Clear error messages for invalid commands or incorrect usage.
- Input/output redirection using `>` and `>>`.
- File compression using `zip` and `unzip`.

---

## 🧰 Supported Commands

| Command | Description | Example |
|----------|--------------|----------|
| `echo` | Prints text to the terminal. | `echo Hello World` |
| `pwd` | Prints the current working directory. | `pwd` |
| `ls` | Lists all files and directories in the current folder. | `ls` |
| `cd` | Changes the current working directory. | `cd C:\Users\abdallah` |
| `mkdir` | Creates a new directory. | `mkdir newFolder` |
| `rmdir` | Deletes an empty directory. Supports `*` to remove all empty directories. | `rmdir "C:\path\to\dir"` or `rmdir *` |
| `touch` | Creates a new empty file. | `touch file.txt` |
| `rm` | Deletes a file. | `rm old.txt` |
| `cat` | Displays the content of a file. | `cat myfile.txt` |
| `wc` | Displays the number of lines, words, and characters in a file. | `wc myfile.txt` |
| `zip` | Compresses one or more files into a `.zip` archive. | `zip archive.zip file1.txt file2.txt` |
| `unzip` | Extracts files from a `.zip` archive. | `unzip archive.zip` |
| `help` | Lists all available commands with usage information. | `help` |

---

## 🔄 Output Redirection

The interpreter supports redirecting output to files:

| Operator | Description | Example |
|-----------|--------------|----------|
| `>` | Redirects output **and overwrites** the file. | `echo Hello > file.txt` |
| `>>` | Redirects output **and appends** to the file. | `echo World >> file.txt` |

**Examples:**
```bash
echo Hello World > myfile.txt      # creates myfile.txt
echo Appending line >> myfile.txt  # appends to myfile.txt
ls > files.txt                     # saves directory listing
