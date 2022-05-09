/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

grammar LibiEL;
/* version 1.0
 */

/*
 * Parser Rules
 */

expression
  : vs EOF
  ;

vs
  : vsFilter
  ;

vsFilter
  : vsAll (Filter vsAll)*
  ;

vsAll
  : (All)? vsSum
  ;

vsSum
  : vsIntersect ((Plus | Minus) vsIntersect)*
  ;

vsIntersect
  : vsUnaryFlow (And vsUnaryFlow)*
  ;

vsUnaryFlow
  : vsFlow (RArrow | RLongArrow | LArrow | LLongArrow)*
  ;

vsFlow
  : vsNot ((RArrow | RLongArrow | LArrow | LLongArrow) vsNot)*
  ;

vsNot
  : (Not)? vsAtom
  ;

vsSelector
  : vsId
  | vsRe
  | vsGId
  | vsGRe
  | vsCurrent
  ;

vsId
  : Id string
  ;

vsRe
  : Re string
  ;

vsGId
  : GId string
  ;

vsGRe
  : GRe string
  ;

vsCurrent
  : Dot
  ;

vsAtom
   : vsSelector
   | vsFunction
   | LParen vs RParen
   ;

vsFunction
  : Word LParen (vsFunctionArgument (Comma vsFunctionArgument)*)? RParen
  | Word
  ;

vsFunctionArgument
  : vs
  | string
  ;

string
  : StringLiteral
  ;

/*
 * Lexer Rules
 */
StringLiteral :	'\'' StringCharacters? '\'';
Id : 'id';
Re : 're';
GId : 'gid';
GRe : 'gre';
Filter : 'filter';
All : 'all';
Not : 'not';

And : '&';
Plus : '+';
Minus : '-';
RArrow : '->';
RLongArrow : '-->';
LArrow : '<-';
LLongArrow : '<--';
LParen : '(';
RParen : ')';
Comma : ',';
Dot : '.';

Word : (Lowercase | Uppercase | '_' )+ ;

Whitespace : [ \t]+ -> skip;

fragment Lowercase          : [a-z] ;
fragment Uppercase          : [A-Z] ;
fragment StringCharacters   :	StringCharacter+;
fragment StringCharacter    :	~["'\\] | EscapeSequence;
fragment EscapeSequence     :	'\\\'';
