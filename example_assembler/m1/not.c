#include <stdio.h>
#include <stdlib.h>

int main(){
    int c = 10;
    if(!c){
        puts("a");
    } else {
        puts("b");
    }
    int d = !c;
    printf("%d\n",d);
}