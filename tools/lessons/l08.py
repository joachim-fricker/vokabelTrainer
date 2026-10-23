from lib import make
groups = [
("Herkunft & Wohnort", """
de_onde|de onde|woher|adv|De onde você é? = Woher kommst du?
pais_land|o país|das Land (Staat)|n
cidade|a cidade|die Stadt|n
alemanha|a Alemanha|Deutschland|n
suica|a Suíça|die Schweiz|n
austria|a Áustria|Österreich|n
brasil|o Brasil|Brasilien|n
brasileiro|brasileiro|brasilianisch; der Brasilianer|adj|Frau: brasileira
suico|suíço|schweizerisch; der Schweizer|adj|Frau: suíça
austriaco|austríaco|österreichisch; der Österreicher|adj|Frau: austríaca
estrangeiro|estrangeiro|ausländisch; der Ausländer|adj|no estrangeiro = im Ausland
morar|morar|wohnen|v
"""),
("Familie & Freunde", """
familia|a família|die Familie|n
pais_eltern|os pais|die Eltern|n
pai|o pai|der Vater|n
mae|a mãe|die Mutter|n
filho|o filho|der Sohn|n|os filhos = die Kinder
filha|a filha|die Tochter|n
irmao|o irmão|der Bruder|n|os irmãos = die Geschwister
irma|a irmã|die Schwester|n
marido|o marido|der Ehemann|n
esposa|a esposa|die Ehefrau|n|Umgangssprachlich auch: a mulher
namorado|o namorado|der feste Freund|n
namorada|a namorada|die feste Freundin|n
avo_m|o avô|der Grossvater|n
avo_f|a avó|die Grossmutter|n|os avós = die Grosseltern
amigo|o amigo|der Freund (Kumpel)|n|die Freundin: a amiga
casado|casado|verheiratet|adj
solteiro|solteiro|ledig|adj
"""),
("Arbeit & Studium", """
trabalho|o trabalho|die Arbeit|n
trabalhar|trabalhar|arbeiten|v
profissao|a profissão|der Beruf|n
empresa|a empresa|die Firma|n
engenheiro|o engenheiro|der Ingenieur|n|a engenheira
professor|o professor|der Lehrer|n|a professora
aposentado|aposentado|pensioniert|adj
estudar|estudar|studieren; lernen|v
estudante|o estudante|der Student|n|Auch: a estudante
"""),
("Freizeit & Interessen", """
gostar|gostar de|mögen, gern haben|v
ler|ler|lesen|v
musica|a música|die Musik|n
esporte|o esporte|der Sport|n
futebol|o futebol|der Fussball|n
passatempo|o passatempo|das Hobby|n
viagem|a viagem|die Reise|n
ferias|as férias|die Ferien, der Urlaub|n|Immer Mehrzahl.
conhecer|conhecer|kennen; kennenlernen|v
"""),
("Eindrücke & Eigenschaften", """
legal|legal|toll, cool; nett|adj|Que legal! = Wie toll!
simpatico|simpático|sympathisch, freundlich|adj
bonito|bonito|schön, hübsch|adj
interessante|interessante|interessant|adj
chato|chato|langweilig; nervig|adj
feliz|feliz|glücklich|adj
idade|a idade|das Alter|n
"""),
("Personen (Pronomen)", """
ele|ele|er|pron
ela|ela|sie (Einzahl)|pron
nos|nós|wir|pron
a_gente|a gente|wir (umgangssprachlich)|pron|Verb wie bei ele/ela: a gente vai
eles|eles, elas|sie (Mehrzahl)|pron
"""),
]
sentences = [
("De onde você é?","Woher kommst du?","de_onde voce","ser_estar"),
("Eu sou da Alemanha.","Ich komme aus Deutschland.","eu alemanha","ser_estar praep_artikel"),
("Sou suíço, de Zurique.","Ich bin Schweizer, aus Zürich.","suico","ser_estar"),
("Ela é austríaca.","Sie ist Österreicherin.","ela austriaco","ser_estar"),
("Ele é brasileiro, de Salvador.","Er ist Brasilianer, aus Salvador.","ele brasileiro","ser_estar"),
("A Suíça é um país pequeno.","Die Schweiz ist ein kleines Land.","suica pais_land pequeno"),
("O Brasil é muito grande.","Brasilien ist sehr gross.","brasil muito grande","ser_estar"),
("Faz tempo que você mora no Brasil?","Wohnst du schon lange in Brasilien?","voce morar brasil","praep_artikel"),
("Eu moro numa cidade pequena perto de Munique.","Ich wohne in einer kleinen Stadt bei München.","eu morar cidade pequeno perto"),
("É a primeira vez que você vem ao Brasil?","Bist du zum ersten Mal in Brasilien?","voce vir brasil","praep_artikel"),
("Muitos estrangeiros moram aqui.","Hier wohnen viele Ausländer.","estrangeiro morar aqui"),
("Você é casado?","Bist du verheiratet?","voce casado","ser_estar"),
("Não, sou solteiro.","Nein, ich bin ledig.","nao solteiro","ser_estar"),
("Esta é a minha esposa, Maria.","Das ist meine Frau, Maria.","esposa este","possessiv demonstrativ"),
("Este é o meu marido.","Das ist mein Mann.","marido este","possessiv demonstrativ"),
("Você tem filhos?","Hast du Kinder?","voce filho ter","ter_haben"),
("Tenho um filho e uma filha.","Ich habe einen Sohn und eine Tochter.","filho filha ter","ter_haben"),
("Meus pais moram na Áustria.","Meine Eltern wohnen in Österreich.","pais_eltern morar austria","possessiv praep_artikel"),
("Meu pai é aposentado.","Mein Vater ist pensioniert.","pai aposentado","possessiv ser_estar"),
("Minha mãe está muito feliz.","Meine Mutter ist sehr glücklich.","mae muito feliz","possessiv ser_estar"),
("Tenho dois irmãos e uma irmã.","Ich habe zwei Brüder und eine Schwester.","num_2 irma ter","ter_haben"),
("Meu irmão mora em Berlim.","Mein Bruder wohnt in Berlin.","irmao morar","possessiv"),
("A minha família é grande.","Meine Familie ist gross.","familia grande","possessiv ser_estar"),
("Ela é minha namorada.","Sie ist meine Freundin.","ela namorada","possessiv"),
("O namorado dela é muito simpático.","Ihr Freund ist sehr sympathisch.","namorado muito simpatico","possessiv"),
("Meu avô tem noventa anos.","Mein Grossvater ist neunzig Jahre alt.","avo_m num_90 ano","possessiv"),
("A minha avó cozinha muito bem.","Meine Grossmutter kocht sehr gut.","avo_f muito bem","possessiv"),
("Ele é um bom amigo.","Er ist ein guter Freund.","ele amigo","ser_estar"),
("O que você faz?","Was machst du beruflich?","o_que voce"),
("Qual é a sua profissão?","Was ist Ihr Beruf?","qual profissao","possessiv"),
("Eu sou engenheiro.","Ich bin Ingenieur.","eu engenheiro","ser_estar"),
("Ela é professora de inglês.","Sie ist Englischlehrerin.","ela professor","ser_estar"),
("Eu trabalho numa empresa de software.","Ich arbeite in einer Softwarefirma.","eu trabalhar empresa"),
("O trabalho é interessante, mas cansativo.","Die Arbeit ist interessant, aber anstrengend.","trabalho interessante"),
("Eu já sou aposentado.","Ich bin schon pensioniert.","eu aposentado","ser_estar"),
("Ele é estudante e estuda medicina.","Er ist Student und studiert Medizin.","ele estudante estudar","ser_estar"),
("Eu estou estudando português.","Ich lerne Portugiesisch.","eu estudar portugues","ser_estar"),
("Estou de férias.","Ich bin im Urlaub.","ferias","ser_estar"),
("A viagem foi ótima!","Die Reise war toll!","viagem"),
("Quanto tempo você vai ficar?","Wie lange bleibst du?","voce ficar ir","fragewoerter ir_futuro"),
("Você gosta de futebol?","Magst du Fussball?","voce gostar futebol"),
("Eu gosto muito de música brasileira.","Ich mag brasilianische Musik sehr.","eu gostar muito musica brasileiro","adjektive"),
("Qual é o seu passatempo?","Was ist dein Hobby?","qual passatempo","possessiv"),
("Eu gosto de ler e de fazer esporte.","Ich lese gern und mache gern Sport.","eu gostar ler esporte"),
("Prazer em conhecer você!","Schön, dich kennenzulernen!","prazer conhecer voce"),
("Você conhece o Rio?","Kennst du Rio?","voce conhecer"),
("Que legal!","Wie toll!","legal"),
("A cidade é muito bonita.","Die Stadt ist sehr schön.","cidade muito bonito"),
("O filme foi muito chato.","Der Film war sehr langweilig.","muito chato"),
("Quantos anos você tem?","Wie alt bist du?","voce ano ter","fragewoerter ter_haben"),
("A idade não importa.","Das Alter spielt keine Rolle.","idade nao"),
("A gente vai à praia amanhã.","Wir gehen morgen an den Strand.","a_gente amanha ir","a_gente ir_futuro"),
("A gente se vê!","Wir sehen uns!","a_gente","a_gente"),
("Nós somos da Suíça.","Wir sind aus der Schweiz.","nos suica","ser_estar praep_artikel"),
("Eles são muito simpáticos.","Sie sind sehr sympathisch.","eles muito","ser_estar"),
("Elas estão cansadas.","Sie sind müde.","eles","ser_estar"),
]
grammar = [
 {"id":"ser_estar","title":"ser und estar – zweimal „sein“",
  "explanation":"ser – für Dauerhaftes: Herkunft, Beruf, Eigenschaften, Zeit\n• eu sou, você/ele/ela é, nós somos, vocês/eles são\n• Sou alemão. Ela é professora. O Brasil é grande.\n\nestar – für Vorübergehendes: Zustand, Befinden, Ort\n• eu estou, você/ele/ela está, nós estamos, vocês/eles estão\n• Estou cansado. Estou de férias. Estou no hotel.\n\nVergleich:\n• Ele é chato. – Er ist (ein) langweilig(er Mensch).\n• Ele está chato hoje. – Er nervt heute.\n\nIm Gespräch wird estar oft verkürzt: tô (estou), tá (está).",
  "examples":[{"text":"Eu sou da Suíça.","de":"Ich bin aus der Schweiz."},{"text":"Eu estou no Brasil.","de":"Ich bin (gerade) in Brasilien."},{"text":"A sopa é boa, mas está fria.","de":"Die Suppe ist gut, aber (gerade) kalt."}]},
 {"id":"possessiv","title":"mein, dein, sein: meu, minha, seu …",
  "explanation":"Das Possessivpronomen richtet sich nach dem Besitz, nicht nach dem Besitzer:\n\n• meu/minha – mein(e): meu pai, minha mãe\n• seu/sua – dein(e), Ihr(e): seu passaporte, sua mala\n• nosso/nossa – unser(e)\n\nMehrzahl: meus, minhas, seus, suas, nossos, nossas.\n\nOft steht der Artikel davor: o meu carro, a minha casa – beides ist richtig.\n\n„sein/ihr“ (von ihm/ihr) sagt man in Brasilien meist mit dele/dela, damit es nicht mit „dein“ verwechselt wird:\no carro dele – sein Auto, a casa dela – ihr Haus.",
  "examples":[{"text":"Minha irmã mora aqui.","de":"Meine Schwester wohnt hier."},{"text":"Qual é o seu nome?","de":"Wie ist dein/Ihr Name?"},{"text":"O marido dela é brasileiro.","de":"Ihr Mann ist Brasilianer."}]},
 {"id":"a_gente","title":"a gente – „wir“ im Alltag",
  "explanation":"Statt nós (wir) sagt man in Brasilien im Gespräch sehr oft a gente. Das Verb steht dann in der Form für ele/ela (3. Person Einzahl):\n\n• nós vamos = a gente vai – wir gehen\n• nós somos = a gente é – wir sind\n• nós temos = a gente tem – wir haben\n\nIm Schriftlichen und in formellen Situationen bleibt nós üblich.",
  "examples":[{"text":"A gente vai jantar fora.","de":"Wir gehen auswärts essen."},{"text":"A gente mora em Zurique.","de":"Wir wohnen in Zürich."},{"text":"A gente se fala!","de":"Wir sprechen uns!"}]},
]
wg = {"a_gente":["a_gente"],"nos":["a_gente"],"de_onde":["ser_estar"]}
make('08_smalltalk.json','pt-BR-08',8,'Smalltalk: Herkunft, Familie, Beruf','Conversa: origem, família e trabalho',groups,sentences,grammar,wg)
