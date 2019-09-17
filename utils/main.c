#include <assert.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/stat.h>

#include "elf.h"

//#define DEBUG

int main(int argc, char* argv[]){
    struct stat st;
    FILE *fp;
    int file_size;
    uint8_t* file_buffer;

    char* file_name = argv[1];

    if(stat(file_name, &st) != 0){
        printf("Failed to get file size :%s\n", file_name);
        exit(1);
    }
    file_size = st.st_size;

    if((fp = fopen(file_name, "rb")) == NULL){
        printf("Failed to open file :%s\n", file_name);
        exit(1);
    }

    file_buffer = malloc(file_size);

    int load_size = fread(file_buffer, sizeof(uint8_t), file_size, fp);
    if(load_size != file_size){
        printf("File size is not matched: %s\n", file_name);
        exit(1);
    }
#ifdef DEBUG
    printf("Loaded File: %s (%dbyte)\n", file_name, load_size);
#endif

    Elf32_Ehdr *Ehdr = (Elf32_Ehdr *)file_buffer;
    if(!IS_ELF(*Ehdr)){
        printf("Unkown file format\n");
        exit(1);
    }
    if(!IS_ELF32(*Ehdr)){
        printf("Not ELF32 format\n");
        exit(1);
    }
#ifdef DEBUG
    printf("Type:ELF32\n");
    printf("Entry point:%d\n\n", Ehdr->e_entry);
#endif

    Elf32_Shdr *Shdr = (Elf32_Shdr *)(file_buffer + Ehdr->e_shoff);
    char *shstr = file_buffer + Shdr[Ehdr->e_shstrndx].sh_offset;
    for(int i=0; i<Ehdr->e_shnum;i++){
        char *name = &shstr[Shdr[i].sh_name];
#ifdef DEBUG
        printf("Shdr:%d sh_name:%s\n", i, &shstr[Shdr[i].sh_name]);
        printf("Shdr:%d sh_type:%04X\n", i, Shdr[i].sh_type);
        printf("Shdr:%d sh_flags:%04X\n", i, Shdr[i].sh_flags);
        printf("Shdr:%d sh_addr:%04X\n", i, Shdr[i].sh_addr);
        printf("Shdr:%d sh_offset:%04X\n", i, Shdr[i].sh_offset);
        printf("Shdr:%d sh_size:%04X\n", i, Shdr[i].sh_size);
        puts("");
#endif

        if (strcmp(".text", name) == 0) {   // ROM
            // For now we assume that .text section begins at address 0.
            assert(Shdr[i].sh_addr == 0 && "The beginning address of .text section should be 0.");

            uint8_t *obj = (uint8_t *)(file_buffer+Shdr[i].sh_offset);
            for(int j=0;j<Shdr[i].sh_size;j+=2){
                uint16_t hex = obj[j] + (obj[j+1]<<8);
                printf("%04X %04X\n", j, hex);
            }
            for(int j=0;j<Shdr[i].sh_size;j+=2){
                uint16_t hex = obj[j] + (obj[j+1]<<8);
                printf("0x%04X, ", hex);
            }
            printf("\n");
        }
    }
}
