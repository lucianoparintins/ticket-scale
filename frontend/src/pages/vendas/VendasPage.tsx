import React, { useEffect, useState } from 'react';
import { api } from '../../services/api';

interface VendaPorEvento {
  nomeEvento: string;
  quantidadeVendida: number;
  valorTotal: number;
}

const VendasPage: React.FC = () => {
  const [vendas, setVendas] = useState<VendaPorEvento[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchVendas = async () => {
      try {
        const data = await api.get('/dashboard/vendas-por-evento');
        setVendas(data);
      } catch (err) {
        console.error('Erro ao buscar vendas', err);
      } finally {
        setLoading(false);
      }
    };
    fetchVendas();
  }, []);

  if (loading) return <div>Carregando...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Relatório de Vendas</h1>

      <div className="card">
        <h3>Vendas por Evento</h3>
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
          Lista detalhada da quantidade de ingressos vendidos e receita gerada por evento.
        </p>

        <table className="table">
          <thead>
            <tr>
              <th>Evento</th>
              <th>Qtd. Vendida</th>
              <th>Receita (R$)</th>
            </tr>
          </thead>
          <tbody>
            {vendas.map((venda, index) => (
              <tr key={index}>
                <td>{venda.nomeEvento}</td>
                <td>{venda.quantidadeVendida}</td>
                <td>{venda.valorTotal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
              </tr>
            ))}
            {vendas.length === 0 && (
              <tr>
                <td colSpan={3} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Nenhuma venda registrada.</td>
              </tr>
            )}
          </tbody>
          {vendas.length > 0 && (
            <tfoot>
              <tr>
                <th style={{ backgroundColor: '#f8fafc' }}>Total</th>
                <th style={{ backgroundColor: '#f8fafc' }}>{vendas.reduce((acc, v) => acc + v.quantidadeVendida, 0)}</th>
                <th style={{ backgroundColor: '#f8fafc' }}>
                  {vendas.reduce((acc, v) => acc + v.valorTotal, 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </th>
              </tr>
            </tfoot>
          )}
        </table>
      </div>
    </div>
  );
};

export default VendasPage;
