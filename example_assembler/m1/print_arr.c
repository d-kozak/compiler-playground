#include <stdio.h>
#include <stdlib.h>

int* alloc_arr(int size){
    return malloc(size * sizeof(int));
}

int main() {
    int* arr = alloc_arr(42);
    for(int i = 0; i < 42 ; i++){
        arr[i] = i+1;
        printf("%d\n",arr[i]);
    }
}