from lib import make
groups = [
("Wochentage", """
segunda|segunda-feira|Montag|n|Kurz: segunda
terca|terça-feira|Dienstag|n|Kurz: terça
quarta|quarta-feira|Mittwoch|n|Kurz: quarta
quinta|quinta-feira|Donnerstag|n|Kurz: quinta
sexta|sexta-feira|Freitag|n|Kurz: sexta
sabado|o sábado|Samstag|n
domingo|o domingo|Sonntag|n
dia|o dia|der Tag|n|Männlich, obwohl auf -a!
semana|a semana|die Woche|n
fim_de_semana|o fim de semana|das Wochenende|n
"""),
("Monate & Jahreszeiten", """
janeiro|janeiro|Januar|n
fevereiro|fevereiro|Februar|n
marco|março|März|n
abril|abril|April|n
maio|maio|Mai|n
junho|junho|Juni|n
julho|julho|Juli|n
agosto|agosto|August|n
setembro|setembro|September|n
outubro|outubro|Oktober|n
novembro|novembro|November|n
dezembro|dezembro|Dezember|n
mes|o mês|der Monat|n
verao|o verão|der Sommer|n|In Brasilien von Dezember bis März!
inverno|o inverno|der Winter|n
primavera|a primavera|der Frühling|n
outono|o outono|der Herbst|n
"""),
("Tageszeiten", """
manha|a manhã|der Morgen, der Vormittag|n|de manhã = morgens
tarde_noun|a tarde|der Nachmittag|n|à tarde = nachmittags
madrugada|a madrugada|die frühen Morgenstunden (ca. 0–5 Uhr)|n
cedo|cedo|früh|adv
tarde_adv|tarde|spät|adv
"""),
("Zu Hause & Tagesablauf", """
casa|a casa|das Haus; das Zuhause|n|em casa = zu Hause
acordar|acordar|aufwachen|v
levantar|levantar|aufstehen|v|Meist: levantar-se / me levanto
dormir|dormir|schlafen|v|eu durmo, você dorme
tomar_banho|tomar banho|duschen, baden|ph|In Brasilien duscht man oft zweimal am Tag.
cozinhar|cozinhar|kochen|v
limpar|limpar|putzen|v
lavar|lavar|waschen|v
"""),
("Wichtige Verben", """
ser|ser|sein (dauerhaft)|v
estar|estar|sein (vorübergehend)|v
poder|poder|können, dürfen|v
saber|saber|wissen; können (gelernt)|v
dizer|dizer|sagen|v
ver|ver|sehen|v
dar|dar|geben|v
"""),
("Häufige Wörter", """
sempre|sempre|immer|adv
nunca|nunca|nie|adv
as_vezes|às vezes|manchmal|adv
agora|agora|jetzt|adv
depois|depois|danach, später|adv|depois de = nach
antes|antes|vorher|adv|antes de = vor
ja|já|schon; gleich|adv|Já vou! = Ich komme gleich!
ainda|ainda|noch|adv|ainda não = noch nicht
tambem|também|auch|adv
mas|mas|aber|conj
porque|porque|weil|conj
ou|ou|oder|conj
todo_dia|todo dia|jeden Tag|ph
"""),
]
sentences = [
("Hoje é segunda-feira.","Heute ist Montag.","hoje segunda","datum ser_estar"),
("Na terça-feira eu trabalho até tarde.","Am Dienstag arbeite ich bis spät.","terca eu trabalhar tarde_adv","datum"),
("A feira é toda quarta-feira.","Der Markt ist jeden Mittwoch.","feira quarta","datum"),
("Quinta-feira é feriado.","Donnerstag ist Feiertag.","quinta","datum"),
("Sexta-feira à noite a gente sai.","Freitagabend gehen wir aus.","sexta noite a_gente sair","datum a_gente"),
("No sábado eu durmo até tarde.","Am Samstag schlafe ich lange.","sabado eu dormir tarde_adv","datum unregelmaessig"),
("O domingo é para a família.","Der Sonntag ist für die Familie.","domingo familia"),
("Que dia é hoje?","Welcher Tag ist heute?","dia hoje","datum"),
("Hoje é dia cinco de maio.","Heute ist der fünfte Mai.","hoje dia num_5 maio","datum"),
("Eu vou ficar uma semana.","Ich bleibe eine Woche.","eu ficar semana ir","ir_futuro"),
("O que você vai fazer no fim de semana?","Was machst du am Wochenende?","o_que voce fim_de_semana fazer","ir_futuro"),
("Eu nasci em janeiro.","Ich bin im Januar geboren.","eu janeiro","datum"),
("Em fevereiro é carnaval.","Im Februar ist Karneval.","fevereiro carnaval","datum"),
("Em março ainda faz calor.","Im März ist es noch heiss.","marco ainda calor","datum wetter"),
("Abril é um mês bom para viajar.","Der April ist ein guter Monat zum Reisen.","abril mes viajar"),
("As férias são em julho.","Die Ferien sind im Juli.","ferias julho","datum"),
("Em junho tem festa junina.","Im Juni gibt es die Junifeste.","junho festa","datum ter_haben"),
("Agosto é o mês do meu aniversário.","Im August ist mein Geburtstag.","agosto mes","possessiv"),
("Setembro e outubro são bons meses para ir ao Nordeste.","September und Oktober sind gute Monate für den Nordosten.","setembro outubro ir"),
("Em novembro começa o calor.","Im November beginnt die Hitze.","novembro calor","datum"),
("O Natal é em dezembro, no verão.","Weihnachten ist im Dezember, im Sommer.","dezembro verao","datum praep_artikel"),
("No inverno chove muito em São Paulo?","Regnet es im Winter viel in São Paulo?","inverno chover muito","praep_artikel"),
("A primavera começa em setembro.","Der Frühling beginnt im September.","primavera setembro","datum"),
("No outono as noites são frescas.","Im Herbst sind die Nächte frisch.","outono","praep_artikel"),
("Eu acordo cedo todo dia.","Ich wache jeden Tag früh auf.","eu acordar cedo todo_dia","praesens"),
("Eu me levanto às sete da manhã.","Ich stehe um sieben Uhr morgens auf.","eu levantar num_7 manha","uhrzeit"),
("De manhã eu tomo café.","Morgens trinke ich Kaffee.","manha eu tomar cafe","datum"),
("À tarde eu vou à praia.","Nachmittags gehe ich an den Strand.","tarde_noun eu praia ir","datum praep_artikel"),
("Ele chegou de madrugada.","Er ist mitten in der Nacht angekommen.","ele madrugada chegar"),
("É muito tarde, vamos dormir.","Es ist sehr spät, lass uns schlafen gehen.","muito tarde_adv dormir ir","ir_futuro"),
("Eu tomo banho antes de dormir.","Ich dusche vor dem Schlafengehen.","eu tomar_banho antes dormir"),
("Hoje eu vou cozinhar em casa.","Heute koche ich zu Hause.","hoje eu cozinhar casa ir","ir_futuro"),
("Preciso limpar a casa.","Ich muss das Haus putzen.","limpar casa precisar","ter_que"),
("Onde eu posso lavar roupa?","Wo kann ich Wäsche waschen?","onde eu lavar roupa poder","unregelmaessig"),
("Eu sou alemão e estou no Brasil.","Ich bin Deutscher und bin in Brasilien.","eu alemao brasil ser estar","ser_estar"),
("Posso pagar com cartão?","Kann ich mit Karte bezahlen?","pagar cartao poder","unregelmaessig"),
("Você pode me ajudar?","Kannst du mir helfen?","voce poder ajudar","unregelmaessig"),
("Eu não sei.","Ich weiss es nicht.","eu nao saber","unregelmaessig"),
("Você sabe onde fica o banco?","Weisst du, wo die Bank ist?","voce saber onde ficar","unregelmaessig"),
("Como se diz isso em português?","Wie sagt man das auf Portugiesisch?","como dizer isso portugues","unregelmaessig"),
("O que ele disse?","Was hat er gesagt?","o_que ele dizer"),
("Eu não vejo nada sem óculos.","Ohne Brille sehe ich nichts.","eu nao ver sem oculos","unregelmaessig"),
("Me dá uma água, por favor.","Gib mir bitte ein Wasser.","dar agua por_favor","bestellen"),
("Eu sempre esqueço a chave.","Ich vergesse immer den Schlüssel.","eu sempre chave","praesens"),
("Eu nunca como carne.","Ich esse nie Fleisch.","eu nunca comer carne","praesens"),
("Às vezes eu vou de ônibus.","Manchmal fahre ich mit dem Bus.","as_vezes eu onibus ir","ir_futuro"),
("Agora não posso, depois eu ligo.","Jetzt kann ich nicht, ich rufe später an.","agora depois poder","unregelmaessig"),
("Antes do jantar, a gente toma uma caipirinha.","Vor dem Abendessen trinken wir einen Caipirinha.","antes jantar a_gente tomar caipirinha","a_gente praep_artikel"),
("Você já comeu?","Hast du schon gegessen?","voce ja"),
("Ainda não, estou com fome.","Noch nicht, ich habe Hunger.","ainda nao fome","estar_com"),
("Eu também gosto de praia.","Ich mag auch den Strand.","eu tambem gostar praia"),
("Eu gosto, mas é muito caro.","Es gefällt mir, aber es ist sehr teuer.","eu gostar mas muito caro"),
("Não fui porque estava doente.","Ich bin nicht gegangen, weil ich krank war.","nao porque doente"),
("Café ou chá?","Kaffee oder Tee?","cafe ou cha"),
("Eu estudo português todo dia.","Ich lerne jeden Tag Portugiesisch.","eu estudar portugues todo_dia","praesens"),
("Ela faz tudo muito bem.","Sie macht alles sehr gut.","ela fazer muito bem","unregelmaessig"),
("Desculpa, eu não sei a resposta.","Entschuldigung, ich weiss die Antwort nicht.","desculpa eu nao saber","unregelmaessig"),
]
grammar = [
 {"id":"datum","title":"Wochentage, Monate und Datum",
  "explanation":"Die Wochentage von Montag bis Freitag werden gezählt: segunda-feira (2. Tag), terça-feira, quarta-feira, quinta-feira, sexta-feira – im Alltag ohne „-feira“: na segunda, na sexta.\nSamstag und Sonntag: o sábado, o domingo.\n\n• am Montag: na segunda(-feira), am Samstag: no sábado\n• im Mai: em maio (Monate klein geschrieben, ohne Artikel)\n• Datum: dia cinco de maio (der 5. Mai); der Erste: primeiro de maio\n• Tageszeiten: de manhã (morgens), à tarde (nachmittags), à noite (abends)\n\nAchtung: In Brasilien ist von Dezember bis März Sommer.",
  "examples":[{"text":"Na sexta eu vou viajar.","de":"Am Freitag verreise ich."},{"text":"Hoje é dia vinte de março.","de":"Heute ist der zwanzigste März."},{"text":"De manhã faz frio.","de":"Morgens ist es kalt."}]},
 {"id":"unregelmaessig","title":"Wichtige unregelmässige Verben",
  "explanation":"Diese Verben braucht man ständig (eu / você, ele, a gente / nós / vocês, eles):\n\n• ser: sou / é / somos / são\n• estar: estou / está / estamos / estão\n• ter: tenho / tem / temos / têm\n• ir: vou / vai / vamos / vão\n• fazer: faço / faz / fazemos / fazem\n• poder: posso / pode / podemos / podem\n• querer: quero / quer / queremos / querem\n• saber: sei / sabe / sabemos / sabem\n• dizer: digo / diz / dizemos / dizem\n• ver: vejo / vê / vemos / veem\n• dar: dou / dá / damos / dão\n• dormir: durmo / dorme / dormimos / dormem",
  "examples":[{"text":"Eu posso entrar?","de":"Darf ich hereinkommen?"},{"text":"Eu não sei.","de":"Ich weiss nicht."},{"text":"O que você faz?","de":"Was machst du?"},{"text":"Eu faço o jantar.","de":"Ich mache das Abendessen."}]},
]
wg = {k:["datum"] for k in "segunda terca quarta quinta sexta sabado domingo dia janeiro fevereiro marco abril maio junho julho agosto setembro outubro novembro dezembro manha tarde_noun".split()}
wg.update({k:["unregelmaessig"] for k in "ser estar poder saber dizer ver dar fazer dormir".split() if k!="fazer"})
make('10_alltag.json','pt-BR-10',10,'Alltag & Zeit','Dia a dia e o tempo',groups,sentences,grammar,wg)
