package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String  API_KEY = "&apikey=ecba5782";
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu(){
        System.out.println("Digite o nome da serie ");
        Scanner scLeitura = new Scanner(System.in);
        var nomeSerie = scLeitura.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        //System.out.println("******* Dados  da  série *******");
        //System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {

            json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+")
                    + "&season=" + i + "&apikey=ecba5782");
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
      //x temporadas.forEach(System.out::println);
      // temporadas.forEach(x -> System.out.println(x));    //idem ao metode de referencia System.out::println
      //                        toda vez que tem 1 parametro e chama 1 função com esse patametro pode usar ::


//        for (int i = 0; i < dados.totalTemporadas(); i++) {
//            List<DadosEpisodio> dadosEpisodios = temporadas.get(i).episodios();
//            for (int j = 0; j < dadosEpisodios.size(); j++) {
//                System.out.println(dadosEpisodios.get(j).titulo());
//            }
//        }
//      Substitui os 2 for acima
        //x temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
//      Streams ================================

//      Nesta lista, para evitar o for dentro de for podemos aplicar o flatMap
//      Vamos juntar todos os episodios de todas as temporadas

//        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());
////              .toList();       //gera uma lista dadosEpisodios imutavel, nesse caso o add não funcionaria
//        dadosEpisodios.add(new DadosEpisodio("Teste",3,"10", "2020-01-01"));
//        dadosEpisodios.forEach(System.out::println);

        //      Pegar os Top 5 Episodios

//        System.out.println("*** Os Top 10 episodios  ***");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A" ))
//                .peek(e -> System.out.println("Primeiro filtro N/A" + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())           //fluxo operação intermediaria: ordenação => gerou novo fluxo que pode sofrer outra ação
//                .peek(e -> System.out.println("Ordenação " + e))
//                .limit(10)   //Outra operação : os 3 primeiroa
//                .peek(e -> System.out.println("Limite " + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Mapeamento " + e))
//                .forEach(System.out::println);

        // Criar uma Classe Episódio a partir da Lista
        List<Episodio> episodios = temporadas.stream()
                .flatMap( t -> t.episodios().stream()
                        .map( d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());
        episodios.forEach(System.out::println);

        // Buscar uma primeira referencia de um trecho de título de um episódio digitado
        
        System.out.println("Digite um trecho de titulo do episódio");
        var trechoTitulo = scLeitura.nextLine();

        // Optional vai fucncionar como um container, então vamos ver o que tem dentro dele

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if (episodioBuscado.isPresent()) {
            System.out.println("Episodio buscado! ");
            System.out.println("Temporada " + episodioBuscado.get().getTemporada());
        } else {
            System.out.println("Episodio nao encontrado!");
        }

        // Filtrar por data lançamento

//        System.out.println("Informe a data de inicio (dd/mm/aaaa) : ");
//        String dataSolicitada = scLeitura.nextLine();
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        LocalDate dataBusca = LocalDate.parse(dataSolicitada,formatador);
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach ( e  -> System.out.println(
//                        "Temporada : " + e.getTemporada() + " Episódio: " + e.getTitulo() +
//                                "Data Lancamento: " + e.getDataLancamento().format(formatador)
//                ));
        // Estatistica - média por por temporada

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter( e-> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(episodio -> episodio.getTemporada(),
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        //Usando classe de estatística DoubleSummaryStatistics
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Estatistica - Classe DoubleSummaryStatistics ");
        System.out.println("Media: " + est.getAverage());
        System.out.println("Melhor episodio: " + est.getMax());
        System.out.println("Pior episodio: " + est.getMin());
        System.out.println("Qtde: " + est.getCount());
    }
}
