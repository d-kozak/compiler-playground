#include <stdio.h>
#include <stdlib.h>

void my_assert(int cond){
    if(!cond){
        printf("Assertion failed");
        exit(1);
    }
}

int main(){
    int a = 10;
    int b = 20;
    int c = a == b;
    my_assert(c);
    printf("%d\n",42);
}