from lib import make
groups = [
("Geschäfte", """
loja|a loja|das Geschäft, der Laden|n
mercado|o mercado|der Markt; der Lebensmittelladen|n
supermercado|o supermercado|der Supermarkt|n
feira|a feira|der Wochenmarkt|n|Die „feira livre“ findet meist einmal pro Woche auf der Strasse statt.
padaria|a padaria|die Bäckerei|n|Dort gibt es auch Kaffee und kleine Snacks.
acougue|o açougue|die Metzgerei|n
shopping|o shopping|das Einkaufszentrum|n
caixa|o caixa|die Kasse|n|Auch: a caixa = die Kassiererin
sacola|a sacola|die Tüte, die Tasche|n
"""),
("Kleidung & Accessoires", """
roupa|a roupa|die Kleidung|n
camisa|a camisa|das Hemd|n
camiseta|a camiseta|das T-Shirt|n
calca|a calça|die Hose|n
bermuda|a bermuda|die kurze Hose, die Shorts|n
vestido|o vestido|das Kleid|n
saia|a saia|der Rock|n
casaco|o casaco|die Jacke, der Mantel|n
sapato|o sapato|der Schuh|n
sandalia|a sandália|die Sandale|n
chinelo|o chinelo|der Flip-Flop, der Badelatschen|n
biquini|o biquíni|der Bikini|n
bone|o boné|die Schirmmütze|n
oculos|os óculos de sol|die Sonnenbrille|n|óculos ist immer Mehrzahl: os óculos
bolsa|a bolsa|die Handtasche|n
tamanho|o tamanho|die Grösse|n|P, M, G = klein, mittel, gross
provador|o provador|die Umkleidekabine|n
"""),
("Farben", """
cor|a cor|die Farbe|n
branco|branco|weiss|adj
preto|preto|schwarz|adj
vermelho|vermelho|rot|adj
azul|azul|blau|adj|Mehrzahl: azuis; gleich für m und f
verde|verde|grün|adj
amarelo|amarelo|gelb|adj
cinza|cinza|grau|adj|Unveränderlich: a camisa cinza
marrom|marrom|braun|adj
rosa|rosa|rosa|adj|Unveränderlich
cor_laranja|laranja|orange|adj|Unveränderlich
"""),
("Obst & Gemüse auf dem Markt", """
banana|a banana|die Banane|n
maca|a maçã|der Apfel|n
laranja|a laranja|die Orange|n
abacaxi|o abacaxi|die Ananas|n
manga|a manga|die Mango|n
mamao|o mamão|die Papaya|n
uva|a uva|die Traube|n
limao|o limão|die Limette, die Zitrone|n|In Brasilien meist die grüne Limette.
melancia|a melancia|die Wassermelone|n
maracuja|o maracujá|die Maracuja, die Passionsfrucht|n
acai|o açaí|die Açaí-Beere|n|Wird gefroren als Creme gegessen.
alface|a alface|der Kopfsalat|n
cenoura|a cenoura|die Karotte|n
alho|o alho|der Knoblauch|n
"""),
("Mengen & Grössen", """
grama|o grama|das Gramm|n|Achtung: männlich! duzentos gramas
litro|o litro|der Liter|n
duzia|a dúzia|das Dutzend|n
pacote|o pacote|die Packung|n
lata|a lata|die Dose|n
par|o par|das Paar|n
pequeno|pequeno|klein|adj
grande|grande|gross|adj
medio|médio|mittel|adj
mais|mais|mehr|adv
menos|menos|weniger|adv
outro|outro|ein anderer, noch ein|pron
"""),
("Einkaufen: Verben & Wörter", """
comprar|comprar|kaufen|v
vender|vender|verkaufen|v
custar|custar|kosten|v
experimentar|experimentar|anprobieren; probieren|v
olhar|olhar|schauen|v|Só estou olhando. = Ich schaue nur.
procurar|procurar|suchen|v
promocao|a promoção|das Angebot, der Ausverkauf|n
aberto|aberto|offen, geöffnet|adj
fechado|fechado|geschlossen|adj
novo|novo|neu|adj
este|este, esta|dieser, diese (hier)|pron
esse|esse, essa|dieser, diese (da)|pron
aquele|aquele, aquela|jener, jene (dort)|pron
isso|isso|das (da)|pron
"""),
]
sentences = [
("A que horas a loja abre?","Um wie viel Uhr öffnet der Laden?","loja hora","fragewoerter"),
("O supermercado está aberto no domingo?","Ist der Supermarkt am Sonntag geöffnet?","supermercado aberto"),
("A loja já está fechada.","Der Laden ist schon geschlossen.","loja fechado"),
("Vamos à feira no sábado?","Gehen wir am Samstag auf den Markt?","feira ir","ir_futuro praep_artikel"),
("Na feira as frutas são mais baratas.","Auf dem Markt ist das Obst günstiger.","feira mais barato","vergleich praep_artikel"),
("Eu compro pão na padaria.","Ich kaufe Brot in der Bäckerei.","eu comprar pao padaria","praep_artikel"),
("Meio quilo de carne moída, por favor.","Ein halbes Kilo Hackfleisch, bitte.","quilo carne por_favor"),
("O açougue fica ao lado do mercado.","Die Metzgerei ist neben dem Markt.","acougue mercado ao_lado ficar"),
("Vamos ao shopping comprar roupa.","Wir gehen ins Einkaufszentrum, Kleider kaufen.","shopping comprar roupa ir","ir_futuro"),
("Onde fica o caixa?","Wo ist die Kasse?","onde caixa ficar"),
("Quer uma sacola?","Möchten Sie eine Tüte?","sacola"),
("Só estou olhando, obrigado.","Ich schaue nur, danke.","olhar obrigado"),
("Estou procurando uma camiseta branca.","Ich suche ein weisses T-Shirt.","procurar camiseta branco","adjektive"),
("Posso experimentar essa calça?","Kann ich diese Hose anprobieren?","experimentar esse calca","demonstrativ"),
("Onde fica o provador?","Wo ist die Umkleidekabine?","onde provador ficar"),
("Qual é o seu tamanho?","Welche Grösse haben Sie?","qual tamanho"),
("Tem um tamanho maior?","Gibt es das eine Grösse grösser?","tamanho","vergleich"),
("Essa camisa é pequena demais.","Dieses Hemd ist zu klein.","esse camisa pequeno","demonstrativ"),
("Tem em outra cor?","Gibt es das in einer anderen Farbe?","cor outro"),
("Eu gosto desse vestido vermelho.","Mir gefällt dieses rote Kleid.","eu vestido vermelho","adjektive demonstrativ"),
("A saia azul é mais bonita.","Der blaue Rock ist schöner.","saia azul mais","vergleich adjektive"),
("Esse casaco preto custa quanto?","Wie viel kostet diese schwarze Jacke?","esse casaco preto","demonstrativ"),
("Quanto custa esse par de sapatos?","Wie viel kostet dieses Paar Schuhe?","quanto_custa esse par sapato","demonstrativ"),
("Preciso de um chinelo para a praia.","Ich brauche Flip-Flops für den Strand.","chinelo","precisar_de"),
("Essas sandálias são muito confortáveis.","Diese Sandalen sind sehr bequem.","sandalia muito"),
("Comprei um biquíni verde.","Ich habe einen grünen Bikini gekauft.","biquini verde","adjektive"),
("Esse boné amarelo é do Brasil.","Diese gelbe Mütze ist aus Brasilien.","esse bone amarelo","adjektive"),
("Perdi os meus óculos de sol.","Ich habe meine Sonnenbrille verloren.","perder oculos"),
("A bolsa marrom está em promoção.","Die braune Handtasche ist im Angebot.","bolsa marrom promocao","adjektive"),
("Calças cinza e camisas rosa.","Graue Hosen und rosa Hemden.","calca cinza rosa","adjektive"),
("Uma camiseta laranja, tamanho M.","Ein orangefarbenes T-Shirt, Grösse M.","camiseta cor_laranja tamanho"),
("Uma bermuda média, por favor.","Eine kurze Hose in M, bitte.","bermuda medio por_favor"),
("Uma dúzia de bananas, por favor.","Ein Dutzend Bananen, bitte.","duzia banana por_favor"),
("Quanto custa o quilo da maçã?","Was kostet das Kilo Äpfel?","quanto_custa quilo maca","praep_artikel"),
("Duas laranjas e um abacaxi.","Zwei Orangen und eine Ananas.","num_2 laranja abacaxi","zahlen_geschlecht"),
("Essa manga está madura?","Ist diese Mango reif?","esse manga","demonstrativ"),
("Eu adoro mamão no café da manhã.","Ich liebe Papaya zum Frühstück.","eu mamao cafe_da_manha"),
("Meio quilo de uva, por favor.","Ein halbes Kilo Trauben, bitte.","quilo uva por_favor"),
("Um suco de limão sem açúcar.","Einen Limettensaft ohne Zucker.","suco limao sem acucar"),
("A melancia é grande demais para mim.","Die Wassermelone ist zu gross für mich.","melancia grande"),
("Suco de maracujá é o meu favorito.","Maracujasaft ist mein Lieblingssaft.","suco maracuja"),
("Um açaí pequeno com banana.","Ein kleines Açaí mit Banane.","acai pequeno banana com"),
("Alface, cenoura e alho, por favor.","Salat, Karotten und Knoblauch, bitte.","alface cenoura alho por_favor"),
("Duzentos gramas de queijo.","Zweihundert Gramm Käse.","num_200 grama queijo"),
("Um litro de leite e um pacote de café.","Ein Liter Milch und eine Packung Kaffee.","litro leite pacote cafe"),
("Uma lata de cerveja custa cinco reais.","Eine Dose Bier kostet fünf Reais.","lata cerveja custar num_5 real"),
("Você vende chip de celular aqui?","Verkaufen Sie hier SIM-Karten?","voce vender aqui"),
("Esse é mais barato do que aquele.","Dieses ist günstiger als jenes.","esse mais barato aquele","vergleich demonstrativ"),
("Este aqui custa menos.","Dieses hier kostet weniger.","este aqui menos","demonstrativ vergleich"),
("O que é isso?","Was ist das?","o_que isso","demonstrativ"),
("Aquela loja vende roupa nova.","Jener Laden verkauft neue Kleidung.","aquele loja vender roupa novo","demonstrativ"),
("Pode me dar outro, por favor?","Können Sie mir bitte einen anderen geben?","outro por_favor"),
("Quanto custa tudo?","Wie viel kostet alles zusammen?","quanto_custa"),
("Isso custa mais de cem reais.","Das kostet mehr als hundert Reais.","isso custar mais num_100 real"),
("Quero comprar alguma coisa para a minha família.","Ich möchte etwas für meine Familie kaufen.","comprar"),
("Essa cor fica bem em você.","Diese Farbe steht dir gut.","esse cor bem voce"),
]
grammar = [
 {"id":"adjektive","title":"Adjektive: Endung und Stellung",
  "explanation":"Adjektive stehen meist NACH dem Nomen und passen sich an Geschlecht und Zahl an:\n\n• o vestido vermelho – das rote Kleid\n• a camisa vermelha – das rote Hemd\n• os sapatos pretos, as calças pretas\n\nAdjektive auf -e oder Konsonant haben nur eine Form für m und f: verde, azul, grande → a saia verde, o casaco verde.\n\nEinige Farben ändern sich gar nicht: rosa, laranja, cinza.",
  "examples":[{"text":"um carro branco","de":"ein weisses Auto"},{"text":"uma casa branca","de":"ein weisses Haus"},{"text":"as camisetas azuis","de":"die blauen T-Shirts"}]},
 {"id":"demonstrativ","title":"dieser, jener: este, esse, aquele",
  "explanation":"• este/esta – dieser/diese hier (bei mir)\n• esse/essa – dieser/diese da (bei dir)\n• aquele/aquela – jener/jene dort (weiter weg)\n\nIm brasilianischen Alltag werden este und esse kaum unterschieden; meistens hört man esse/essa.\n\nFür „das“ ohne Nomen: isso (das da), isto (das hier), aquilo (das dort).\n\nMit de verschmelzen sie: de + esse = desse, em + essa = nessa.",
  "examples":[{"text":"Essa camiseta é bonita.","de":"Dieses T-Shirt ist schön."},{"text":"Aquele prédio é um hotel.","de":"Jenes Gebäude ist ein Hotel."},{"text":"Quanto custa isso?","de":"Wie viel kostet das?"}]},
 {"id":"vergleich","title":"Vergleichen: mais … do que",
  "explanation":"• mais … (do) que – mehr … als: mais barato do que – günstiger als\n• menos … (do) que – weniger … als\n• tão … quanto – so … wie\n\nUnregelmässig:\n• bom → melhor (besser)\n• ruim → pior (schlechter)\n• grande → maior (grösser)\n• pequeno → menor (kleiner)\n\n„zu“ (zu gross) heisst demais und steht hinten: grande demais.",
  "examples":[{"text":"Aqui é mais barato do que lá.","de":"Hier ist es günstiger als dort."},{"text":"Tem um tamanho maior?","de":"Gibt es eine grössere Grösse?"},{"text":"É tão caro quanto em casa.","de":"Es ist so teuer wie zu Hause."}]},
]
wg = {"este":["demonstrativ"],"esse":["demonstrativ"],"aquele":["demonstrativ"],"isso":["demonstrativ"],"mais":["vergleich"],"menos":["vergleich"]}
make('06_einkaufen.json','pt-BR-06',6,'Einkaufen','Fazendo compras',groups,sentences,grammar,wg)
