type var = string
type param = 
  | Var of string
  | Const of int

let string_of_param = function
  | Var x -> x
  | Const x -> string_of_int x

type instr = 
  | Mov of var * param
  | Add of var * param * param
  | Print of param

let string_of_instr = function
  | Mov (t,s) -> t ^ " = " ^ string_of_param s
  | Add (t,x,y) -> t  ^ " = " ^ string_of_param x ^ " + " ^ string_of_param y
  | Print c -> "print " ^ string_of_param c

let string_of_prog prog = prog |> List.map string_of_instr |> List.fold_left (fun acc elem -> acc ^ "\n" ^ elem) ""
let print_prog prog = print_endline (string_of_prog prog)

module Scope = Map.Make(String)

let print_scope scope = 
  let print_var key value = print_endline (key ^ " = " ^ string_of_int value) in
    Scope.iter print_var scope 

let eval_prog prog = 
  let eval_param scope = function
    | Const c -> c
    | Var v -> Scope.find v scope 
  in 
  let eval_instr scope = function 
    | Mov (t, p) -> Scope.add t (eval_param scope p) scope 
    | Add (t,x,y) -> Scope.add t (eval_param scope x + eval_param scope y) scope
    | Print p -> begin
      eval_param scope p |> string_of_int |> print_endline; 
      scope 
    end
  in List.fold_left eval_instr Scope.empty prog 
let prog = [
  Mov ("a", Const 10);
  Mov ("b", Const 10);
  Mov ("c", Var "b");
  Add ("d", Var "a", Var "c");
  Print (Var "d")
]

let () = begin
  print_prog prog;
  print_endline "Evaluation:";
  print_scope (eval_prog prog);
end

