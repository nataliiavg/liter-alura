package br.com.natalia.challenge.LiterAlura.principal;

import br.com.natalia.challenge.LiterAlura.model.Autor;
import br.com.natalia.challenge.LiterAlura.model.Livro;
import br.com.natalia.challenge.LiterAlura.model.LivroDTO;
import br.com.natalia.challenge.LiterAlura.repository.LivroRepository;
import br.com.natalia.challenge.LiterAlura.services.ConsultaApi;
import br.com.natalia.challenge.LiterAlura.services.ConverteDados;

import java.time.Year;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Principal {
    private ConsultaApi consultaApi = new ConsultaApi();
    private ConverteDados converteDados = new ConverteDados();
    private Scanner leitura = new Scanner(System.in);
    private LivroRepository repositorio;
    private String endereco = "https://gutendex.com/books/?search=";

    public Principal(LivroRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        int opcao = -1;

        while (opcao != 0) {
            String menu = """
                    ==========================================
                    \nEscolha o número de sua opção:
                    
                    1 - Buscar livro pelo título
                    2 - Listar livros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos em um determinado ano
                    5 - Listar livros em um determinado idioma
                    
                    0 - Sair
                    ==============================================
                    """;
            try {
                System.out.println(menu);
                opcao = leitura.nextInt();
                leitura.nextLine();

                switch (opcao) {
                    case 1:
                        buscarLivro();
                        break;
                    case 2:
                        listarLivros();
                        break;
                    case 3:
                        listarAutores();
                        break;
                    case 4:
                        listarAutoresVivosNoAno();
                        break;
                    case 5:
                        listarLivrosPorIdioma();
                        break;
                    case 0:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("Opção inválida");
                }
            } catch (InputMismatchException e) {
                System.out.println("Opção inválida! Insira um número inteiro.");
                leitura.nextLine();
            }
        }
    }

    private void buscarLivro() {
        System.out.println("Digite o nome do Livro: ");
        var nomeLivro = leitura.nextLine();
        System.out.println("Buscando...");
        String enderecoBusca = endereco.concat(nomeLivro.replace(" ", "+").toLowerCase().trim());

        String json = consultaApi.buscar(enderecoBusca);
        String jsonLivro = converteDados.extraiObjetoJson(json, "results");

        List<LivroDTO> livrosDTO = converteDados.obterLista(jsonLivro, LivroDTO.class);

        if (!livrosDTO.isEmpty()) {
            Livro livro = new Livro(livrosDTO.get(0));

            Autor autor = repositorio.buscarAutorPeloNome(livro.getAutor().getAutor());
            if (autor != null) {
                livro.setAutor(null);
                repositorio.save(livro);
                livro.setAutor(autor);
            }
            livro = repositorio.save(livro);
            System.out.println(livro);
        } else {
            System.out.println("Livro não encontrado");
        }
    }


    private void listarLivros() {
        List<Livro> livros = repositorio.findAll();
        livros.forEach(System.out::println);
    }

    private void listarAutores() {
        List<Autor> autores = repositorio.buscarAutores();
        autores.forEach(System.out::println);
    }

    private void listarAutoresVivosNoAno() {
        try {
            System.out.println("Digite o ano:");
            int ano = leitura.nextInt();
            leitura.nextLine();

            List<Autor> autores = repositorio.buscarAutoresVivosNoAno(Year.of(ano));
            autores.forEach(System.out::println);
        } catch (InputMismatchException e) {
            System.out.println("Erro! Por favor, digite um número inteiro.");
            leitura.nextLine();
        }
    }

    private void listarLivrosPorIdioma() {
        System.out.println("""
                Digite o idioma para busca
                es - espanhol
                en - inglês
                fr - francês
                pt - português
                """);
        String idioma = leitura.nextLine();
        List<Livro> livros = repositorio.findByIdioma(idioma);
        if (!livros.isEmpty()) {
            livros.forEach(System.out::println);
        } else {
            System.out.println("Não exite livros nesse idioma cadastrado");
        }
    }
}

