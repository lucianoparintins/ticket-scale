import React, { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { TrendingUp, ShoppingCart, DollarSign, Users } from 'lucide-react';

interface Metricas {
  totalVendas: number;
  totalReservas: number;
  taxaConversao: number;
  ticketMedio: number;
}

const DashboardPage: React.FC = () => {
  const [metricas, setMetricas] = useState<Metricas | null>(null);
  const [receita, setReceita] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [m, r] = await Promise.all([
          api.get('/dashboard/metricas'),
          api.get('/dashboard/receita-total')
        ]);
        setMetricas(m);
        setReceita(r);
      } catch (err) {
        console.error('Erro ao buscar métricas', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <div>Carregando...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Dashboard</h1>
      
      <div className="grid">
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', backgroundColor: '#dbeafe', color: '#1d4ed8', borderRadius: '0.5rem' }}>
              <DollarSign size={24} />
            </div>
            <div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Receita Total</p>
              <h3 style={{ fontSize: '1.5rem' }}>R$ {receita?.valorTotal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</h3>
            </div>
          </div>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', backgroundColor: '#dcfce7', color: '#15803d', borderRadius: '0.5rem' }}>
              <ShoppingCart size={24} />
            </div>
            <div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Vendas Totais</p>
              <h3 style={{ fontSize: '1.5rem' }}>{metricas?.totalVendas}</h3>
            </div>
          </div>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', backgroundColor: '#fef3c7', color: '#b45309', borderRadius: '0.5rem' }}>
              <TrendingUp size={24} />
            </div>
            <div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Taxa de Conversão</p>
              <h3 style={{ fontSize: '1.5rem' }}>{(metricas?.taxaConversao || 0).toFixed(2)}%</h3>
            </div>
          </div>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', backgroundColor: '#f3e8ff', color: '#7e22ce', borderRadius: '0.5rem' }}>
              <Users size={24} />
            </div>
            <div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Reservas</p>
              <h3 style={{ fontSize: '1.5rem' }}>{metricas?.totalReservas}</h3>
            </div>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginTop: '2rem' }}>
        <h3>Visão Geral do Sistema</h3>
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>
          Bem-vindo ao painel administrativo do TicketScale. Aqui você pode gerenciar eventos, acompanhar vendas e monitorar o desempenho do sistema em tempo real.
        </p>
      </div>
    </div>
  );
};

export default DashboardPage;
